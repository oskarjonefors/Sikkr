package edu.chalmers.sikkr.backend;

import java.util.List;

/**
 * Interface representing a contact.
 */
public interface Contact {

    public String getName();

    /**
     * Return a list of all the contact's phone numbers.
     */
    public List<String> getPhoneNumbers();

}
