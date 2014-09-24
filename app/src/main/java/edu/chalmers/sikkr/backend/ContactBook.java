package edu.chalmers.sikkr.backend;

import java.util.Set;
import java.util.TreeSet;

import android.provider.ContactsContract;
import android.content.Context;
import android.database.Cursor;
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
            final SikkrContact contact = new SikkrContact(name, contact_id);
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
            if (contact.getName().toLowerCase().charAt(0) == initialLetter) {
                c.add(contact);
            }
        }
        return c;
    }

}
