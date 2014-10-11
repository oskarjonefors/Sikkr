package edu.chalmers.sikkr.backend.contact;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import edu.chalmers.sikkr.backend.ProgressListenable;
import edu.chalmers.sikkr.backend.util.ClipartUtility;
import edu.chalmers.sikkr.backend.util.FuzzySearchUtility;
import edu.chalmers.sikkr.backend.util.ProgressListener;

import static android.provider.ContactsContract.CommonDataKinds.*;

/**
 * @author Oskar Jönefors
 */
public class ContactBook implements ProgressListenable {

    private Context context;
    private final Map<String, Contact> contacts = new TreeMap<String, Contact>();

    /* This map uses contact name as key and contact id as value */
    private final Map<String, String> contactNameMap = new TreeMap<String, String>();

    private ClipartUtility cu;
    private Collection<ProgressListener> listeners;

    private final static ContactBook singleton = new ContactBook();

    private ContactBook() {
        listeners = new ArrayList<ProgressListener>();
    }

    private void setup(Context context) {
        this.context = context;
        contacts.clear();
        cu = new ClipartUtility(context);
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);

        double rowCount = (double)cursor.getCount();

        while (cursor.moveToNext()) {

            notifyListeners(cursor.getPosition()/rowCount, "Retrieving contact information.");

            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,Uri.encode(name.trim()));
            Cursor mapContact = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
            if (mapContact.moveToNext()) {
                final String contact_id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts._ID));
                final long longID = Long.valueOf(contact_id);
                final Uri contact_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, longID);
                final SikkrContact contact = new SikkrContact(name, contact_id, getPhoto(contact_uri, longID));
                final Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                        Phone.CONTACT_ID + " = " + contact_id, null, null);
                addPhoneNumbers(contact, phoneNumbers);

                if (contact.getDefaultNumber() != null) {
                    contacts.put(contact_id, contact);
                    contactNameMap.put(name, contact_id);
                }
            }
        }
        cursor.close();
        cu.saveChanges();
    }

    private boolean hasContext() {
        return context != null;
    }

    public static void setupSingleton(Context context) {
        singleton.setup(context);
    }

    /**
     * This method is used if another class want's to monitor the progress of the ContactBook initialization.
     * @param context
     * @param listener
     */
    public static void setupSingleton(Context context, ProgressListener listener) {
        singleton.addProgressListener(listener);
        singleton.setup(context);
    }

    public static ContactBook getSharedInstance() {
        if (singleton.hasContext()) {
            return singleton;
        } else {
            throw new UnsupportedOperationException("This singleton requires a context");
        }
    }

    private void addPhoneNumbers(final SikkrContact contact, final Cursor phoneNumbers) {


        while (phoneNumbers.moveToNext()) {
            final String phNumber = phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.NUMBER));
            final int PHONE_TYPE = phoneNumbers.getInt(phoneNumbers.getColumnIndex(Phone.TYPE));

            if (PHONE_TYPE == Phone.TYPE_HOME) {
                contact.addPhoneNumber(phNumber);
            } else {
                contact.addMobilePhoneNumber(phNumber);
            }
        }
        phoneNumbers.close();
    }

    private Bitmap getPhoto(Uri contactUri, long id) {
            final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, true);
            if (input == null) {
                return cu.getContactImage("" + id);
            } else {
                return BitmapFactory.decodeStream(input);
            }
    }


    /**
     * Get a set of all the contacts' initial letters.
     */
    public Set<Character> getInitialLetters() {
        final Set<Character> letters = new TreeSet<Character>();
        for (final Contact contact : contacts.values()) {
            letters.add(Character.toUpperCase(contact.getName().charAt(0)));
        }
        return letters;
    }

    /**
     * Get a list of all the contacts in the contact book.
     */
    public Set<Contact> getContacts() {
        final Set<Contact> c = new TreeSet<Contact>();
        for (final Contact contact : contacts.values()) {
            c.add(contact);
        }
        return c;
    }

    /**
     * Get all contacts starting with the given initial letter.
     * @param initialLetter a char ranging from 'a' to 'z' and also 'å' 'ä' or 'ö'
     * @return a set of contacts in alphabetic order.
     */
    public Set<Contact> getContacts(char initialLetter) {
        final Set<Contact> c = new TreeSet<Contact>();
        for (final Contact contact : contacts.values()) {
            if (contact.getName().toLowerCase().charAt(0) == Character.toLowerCase(initialLetter)) {
                c.add(contact);
            }
        }
        return c;
    }

    public Contact getContact(String id) {
        return contacts.get(id);
    }

    /**
     * Return the contact who's name best matches the given pattern.
     * If no suitable contacts can be found, null will be returned.
     * @param searchPattern
     * @return
     */
    public Contact getClosestMatch(String searchPattern) {
        return getClosestMatches(searchPattern).get(0);
    }

    /**
     * Return the contacts whos' names best matches the given pattern.
     * @param searchPattern
     * @return
     */
    public List<Contact> getClosestMatches(String searchPattern) {

        List<String> matches = FuzzySearchUtility.getSearchResults(searchPattern, contactNameMap.keySet());

        if (matches == null) {
            return null;
        }

        List<Contact> results = new ArrayList<Contact>();

        for (String str : matches) {
            results.add(getContact(contactNameMap.get(str)));
        }

        return results;
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyListeners(double progress, String taskMsg) {
        for (ProgressListener listener : listeners) {
            listener.notifyProgress(progress, "Contact Book", taskMsg);
        }
    }
}
