package edu.chalmers.sikkr.backend.contact;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.chalmers.sikkr.backend.ProgressListenable;
import edu.chalmers.sikkr.backend.util.ClipartUtility;
import edu.chalmers.sikkr.backend.util.FuzzySearchUtility;
import edu.chalmers.sikkr.backend.util.MessageUtils;
import edu.chalmers.sikkr.backend.util.ProgressListener;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * @author Oskar Jönefors
 */
public class ContactBook implements ProgressListenable {

    private Context context;

    /* Maps a contact ID to a contact */
    private final Map<String, Contact> contacts = new TreeMap<>();

    /* This map uses contact name as key and contact id as value */
    private final Map<String, String> contactNameMap = new TreeMap<>();

    /* Chooses display pictures for the contacts who do not have display pictures. */
    private ClipartUtility clipartUtility;

    /* Used to make other classes aware of it's progress. */
    private final Collection<ProgressListener> listeners;


    private final static ContactBook singleton = new ContactBook();

    private ContactBook() {
        listeners = new ArrayList<>();
    }

    /* These characters are the only valid initial characters that the contacts will be sorted by */
    private final static Character[] VALID_INITIAL_CHARACTERS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'Å', 'Ä', 'Ö' };

    /* All contacts with initial letters not in the above list will be grouped under this character. */
    private final static Character SYMBOL_INITIAL_CHARACTER = '#';

    private void setup(Context context) {
        this.context = context;
        contacts.clear();

        /* This tool assigns display pictures to the contacts who do not have display pictures */
        clipartUtility = new ClipartUtility(context);
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);

        double rowCount = (double)cursor.getCount();

        while (cursor.moveToNext()) {

            /* Notifies all added ProgressListeners of the contact retrieving progress. */
            notifyListeners(cursor.getPosition()/rowCount, "Retrieving contact information.");

            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            final boolean isFavorite = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1;
            final int timesContacted = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
            final long lastTimeContacted = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
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
                    contact.calculatePriority(isFavorite, timesContacted, lastTimeContacted);
                    contact.setFavorite(isFavorite);
                    Log.d("StartActivity", "Priority for " + contact.getName() + " is " + contact.getPriority());
                    contacts.put(contact_id, contact);
                    contactNameMap.put(name, contact_id);
                }
            }
            mapContact.close();
        }
        cursor.close();
        clipartUtility.saveChanges();
    }

    private boolean hasContext() {
        return context != null;
    }

    /**
     * This method is used if another class wants to monitor the progress of the ContactBook initialization.
     * @param context   - Context used for retrieve the contacts. Cannot be null.
     * @param listener  - A ProgressListener that wants to monitor the class initialization.
     */
    public static void setupSingleton(Context context, ProgressListener listener) {

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null!");
        }

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
                return clipartUtility.getContactImage("" + id);
            } else {
                return BitmapFactory.decodeStream(input);
            }
    }


    /**
     * Get a set of all the contacts' initial letters.
     */
    public Set<Character> getInitialLetters() {

        Set<Character> validChars = new HashSet<>(Arrays.asList(VALID_INITIAL_CHARACTERS));

        final Set<Character> letters = new TreeSet<>();
        for (final Contact contact : contacts.values()) {
            Character c = Character.toUpperCase(contact.getName().charAt(0));

            if (validChars.contains(c)) {
                letters.add(c);
            } else {
                letters.add(SYMBOL_INITIAL_CHARACTER);
            }
        }
        return letters;
    }

    /**
     * Get a list of all the contacts in the contact book.
     */
    Set<Contact> getContacts() {
        final Set<Contact> c = new TreeSet<>();
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
        final Set<Contact> c = new TreeSet<>();
        final List<Character> validChars = Arrays.asList(VALID_INITIAL_CHARACTERS);

        for (final Contact contact : contacts.values()) {
            final char cl = contact.getName().toUpperCase().charAt(0);

            if (validChars.contains(cl) && cl == Character.toUpperCase(initialLetter)) {
                c.add(contact);
            } else if (!validChars.contains(cl) && initialLetter == SYMBOL_INITIAL_CHARACTER) {
                c.add(contact);
            }
        }
        return c;
    }

    public Contact getContact(String id) {
        return contacts.get(id);
    }

    public Contact getFirstContactByName(CharSequence name) {
        for (Contact contact : contacts.values()) {
            if (contact.getName().trim().equals(name)) {
                return contact;
            }
        }
        return null;
    }

    /**
     * Return the contact who's name best matches the given pattern.
     * If no suitable contacts can be found, null will be returned.
     *
     * @param searchPattern  If search pattern is null, null will be returned.
     *
     * @return  A matching contact if found, or null if none is found.
     */
    public Contact getClosestMatch(String searchPattern) {
        final List<Contact> matches = getClosestMatches(searchPattern);
        if (matches == null) {
            return null;
        }

        Contact topContact = matches.get(0);
        long topPriority = topContact.getPriority();

        for (Contact c : matches) {
            if (c.getPriority() > topPriority) {
                topContact = c;
            }
        }

        return topContact;
    }

    /**
     * Return the contacts who's names best matches the given pattern.
     *
     * @param searchPattern If search pattern is null, null will be returned.
     * @return  List of matching contacts. This list will be empty if none are found.
     */
    public List<Contact> getClosestMatches(String searchPattern) {

        List<String> matches = FuzzySearchUtility.getSearchResults(searchPattern, contactNameMap.keySet());

        if (matches == null) {
            return null;
        }

        List<Contact> results = new ArrayList<>();

        for (String str : matches) {
            results.add(getContact(contactNameMap.get(str)));
        }
        return results;
    }

    Set<Contact> getFavoriteContacts() {
        final Set<Contact> favSet = new TreeSet<>();

        for (Contact c : contacts.values()) {
            if (c.isFavorite()) {
                favSet.add(c);
            }
        }

        return favSet;
    }

    public List<Contact> getTopContacts(int nbrOfContacts) {
        final List<Contact> topList = new ArrayList<>();
        final List<Contact> favList = new ArrayList<>();
        favList.addAll(getFavoriteContacts());
        Collections.sort(favList, new PriorityComparator());

        if (favList.size() >= nbrOfContacts) {
            for (int i = 0; i < nbrOfContacts; i++) {
                topList.add(favList.iterator().next());
            }
            return topList;
        } else {
            for (Contact contact : favList) {
                topList.add(contact);
            }

            final List<Contact> allList = new ArrayList<>();
            allList.addAll(getContacts());

            Collections.sort(allList, new PriorityComparator());
            int emptySlots = Math.min(nbrOfContacts - topList.size(), allList.size());

            for(int i = 0; i < emptySlots; i++) {
                if (!topList.contains(allList.get(i))) {
                    topList.add(allList.get(i));
                }
            }

            Collections.sort(topList, new PriorityComparator());
            return topList;
        }
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

    private static class PriorityComparator implements Comparator<Contact> {

        @Override
        public int compare(Contact o1, Contact o2) {
            return Long.compare(o2.getPriority(), o1.getPriority());
        }

    }
}
