package edu.chalmers.sikkr.backend;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;
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
    private final Set<Contact> contacts = new TreeSet<Contact>();;
    private final static ContactBook singleton = new ContactBook();

    private ContactBook() { }

    public void setup(Context context) {
        this.context = context;
        contacts.clear();
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            final String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            final long longID = Long.valueOf(contact_id);

            final Uri contact_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, longID);
            Log.d("Contacts", "Added contact with name: " + name + ", " + "id: " + contact_id + " and picture: " + getPhoto(contact_uri));
            final SikkrContact contact = new SikkrContact(name, contact_id, getPhoto(contact_uri));
            final Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + " = " + contact_id, null, null);
            contacts.add(contact);
            addPhoneNumbers(contact, phoneNumbers);
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
        for (final Contact contact : contacts) {
            letters.add(contact.getName().charAt(0));
        }
        return letters;
    }

    /**
     * Get a list of all the contacts in the contact book.
     */
    public Set<Contact> getContacts() {
        return contacts;
    }

    /**
     * Get all contacts starting with the given initial letter.
     * @param initialLetter a char ranging from 'a' to 'z' and also 'å' 'ä' or 'ö'
     * @return
     */
    public Set<Contact> getContacts(char initialLetter) {
        final Set<Contact> c = new TreeSet<Contact>();
        for (final Contact contact : contacts) {
            if (contact.getName().toLowerCase().charAt(0) == Character.toLowerCase(initialLetter)) {
                c.add(contact);
            }
        }
        return c;
    }

}
