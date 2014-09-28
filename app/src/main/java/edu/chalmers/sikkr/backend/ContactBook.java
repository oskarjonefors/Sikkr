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

    private final Context context;
    private final Set<Contact> contacts;

    public ContactBook(final Context context) {
        this.context = context;
        contacts = new TreeSet<Contact>();
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
            final String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            final long longID = Long.valueOf(contact_id);

            final Uri contact_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, longID);
            Log.d("ContactBook", "CONTACT URI IS " + contact_uri);

            final Uri photo_uri = Uri.withAppendedPath(contact_uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

            final Bitmap photo = getPhoto(photo_uri);
            //TODO: If the contact photos do not exist, choose a unique clipart photo for the contact.

            final SikkrContact contact = new SikkrContact(name, contact_id, photo);
            final Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + " = " + contact_id, null, null);

            contacts.add(contact);
            addPhoneNumbers(contact, phoneNumbers);
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
    }

    private Bitmap getPhoto(Uri uri) {
        try {
            final InputStream input = context.getContentResolver().openInputStream(uri);
            Log.d("getPhoto", "Input = " + input + " for " + uri);
            if (input == null) {
                return null;
            } else {
                return BitmapFactory.decodeStream(input);
            }
        } catch (FileNotFoundException e) {
            Log.d("getPhoto", "FILE NOT FOUND");
            return null;
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
