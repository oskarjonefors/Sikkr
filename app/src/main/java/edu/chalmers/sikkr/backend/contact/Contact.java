package edu.chalmers.sikkr.backend.contact;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Interface representing a contact.
 */
public interface Contact extends Comparable<Contact> {

    /**
     * Returns the full-sized contact photo. If no such photo exists, null is returned.
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

    /**
     * Return the contact's default number.
     */
    String getDefaultNumber();

    /**
     * Get the priority of the contact, where higher number means more prioritized. This
     * @return
     */
    long getPriority();

    /**
     * Calculate and set a long value that determines the priority of the contact for shows in contact views.
     * Higher is more prioritized, and the value is calculated from the three given parameters.
     *
     * @param isFavorite        - Whether or not the contact is marked as favorite.
     * @param timesContacted    - How many times the contact has been contacted.
     * @param lastContacted     - The long time in milliseconds for when the contact was last contacted.
     */
    void calculatePriority(boolean isFavorite, int timesContacted, long lastContacted);
}
