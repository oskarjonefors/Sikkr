package edu.chalmers.sikkr.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.provider.ContactsContract;
import android.content.Context;
import android.database.Cursor;
import static android.provider.ContactsContract.CommonDataKinds.*;

/**
 * @author Oskar Jönefors
 */
public class ContactBook {

    private final Context context;
    private final List<Contact> contacts;

    public ContactBook(Context context) {
        this.context = context;
        contacts = new ArrayList<Contact>();
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
            final SikkrContact contact = new SikkrContact(name);

            contacts.add(contact);
            addPhoneNumbers(contact, cursor);
        }
    }

    private void addPhoneNumbers(SikkrContact contact, Cursor cursor) {
        final String contact_Id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        final Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                Phone.CONTACT_ID + " = " + contact_Id, null, null);
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
        final Set<Character> letters = new HashSet<Character>();
        for (final Contact contact : contacts) {
            letters.add(contact.getName().charAt(0));
        }
        return letters;
    }

    /**
     * Get a list of all the contacts in the contact book.
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * Get all contacts starting with the given initial letter.
     * @param initialLetter a char ranging from 'a' to 'z' and also 'å' 'ä' or 'ö'
     * @return
     */
    public List<Contact> getContacts(char initialLetter) {
        final List<Contact> c = new ArrayList<Contact>();
        for (final Contact contact : contacts) {
            if (contact.getName().toLowerCase().charAt(0) == initialLetter) {
                c.add(contact);
            }
        }
        return c;
    }

}
