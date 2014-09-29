package edu.chalmers.sikkr.backend;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
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

import static android.provider.ContactsContract.CommonDataKinds.*;

/**
 * @author Oskar Jönefors
 */
public class ContactBook {

    private Context context;
    private final Map<String, Contact> contacts = new TreeMap<String, Contact>();;
    private final static ContactBook singleton = new ContactBook();

    private ContactBook() { }

    public void setup(Context context) {
        this.context = context;
        contacts.clear();
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,Uri.encode(name.toString().trim()));
            Cursor mapContact = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
            if (mapContact.moveToNext()) {
                final String contact_id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts._ID));
                final long longID = Long.valueOf(contact_id);
                final Uri contact_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, longID);
                final SikkrContact contact = new SikkrContact(name, contact_id, getPhoto(contact_uri));
                final Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                        Phone.CONTACT_ID + " = " + contact_id, null, null);
                contacts.put(contact_id, contact);
                addPhoneNumbers(contact, phoneNumbers);
            }
        }
        cursor.close();
    }

    public boolean hasContext() {
        return context != null;
    }

    public static void setupSingleton(Context context) {
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
                break;
            } else {
                contact.addMobilePhoneNumber(phNumber);
                break;
            }
        }
        phoneNumbers.close();
    }

    private Bitmap getPhoto(Uri contactUri) {
            final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, true);
            if (input == null) {
                return null;
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
     * @return
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
}
