package edu.chalmers.sikkr.backend;

import java.util.ArrayList;
import java.util.List;
import android.provider.ContactsContract;
import android.content.Context;
import android.database.Cursor;
import static android.provider.ContactsContract.CommonDataKinds.*;

/**
 * @author Oskar Jönefors
 */
public abstract class ContactBook {

    private final Context context;
    private final List<SikkrContact> contacts;

    public ContactBook(Context context) {
        this.context = context;
        contacts = new ArrayList<SikkrContact>();
        final Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
            final SikkrContact contact = new SikkrContact(name);

            contacts.add(contact);
            addPhoneNumbers(contact, cursor);
        }
    }

    private void addPhoneNumbers(SikkrContact contact, Cursor cursor) {
        String contact_Id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        Cursor phoneNumbers = context.getContentResolver().query(Phone.CONTENT_URI, null,
                Phone.CONTACT_ID + " = " + contact_Id, null, null);
        while (phoneNumbers.moveToNext()) {
            String phNumber = phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.NUMBER));
            int PHONE_TYPE = phoneNumbers.getInt(phoneNumbers.getColumnIndex(Phone.TYPE));

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
     * Get a list of all the contacts' initial letters.
     */
    public abstract List<Character> getInitialLetters();

    /**
     * Get a list of all the contacts in the contact book.
     */
    public abstract List<Contact> getContacts();

    /**
     * Get all contacts starting with the given initial letter.
     * @param initialLetter a char ranging from 'a' to 'z' and also 'å' 'ä' or 'ö'
     * @return
     */
    public abstract List<Contact> getContacts(char initialLetter);

}
