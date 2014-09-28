package edu.chalmers.sikkr.backend;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Interface representing a contact.
 */
public interface Contact extends Comparable<Contact> {

    /**
     * Returns the thumbnail-sized contact photo. If no such photo exists, an empty Bitmap is returned.
     */
    Bitmap getThumbnail();

    /**
     * Returns the full-sized contact photo. If no such photo exists, an empty Bitmap is returned.
     */
    Bitmap getPhoto();

    /**
     * Returns the internal representation of the contact.
     */
    String getID();

    /**
     * Returns the name to be shown to the user.
     */
    String getName();

    /**
     * Return a list of all the contact's phone numbers.
     */
    List<String> getPhoneNumbers();

    /**
     * Return a list of all the contact's mobile phone numbers.
     */
    List<String> getMobilePhoneNumbers();

}
