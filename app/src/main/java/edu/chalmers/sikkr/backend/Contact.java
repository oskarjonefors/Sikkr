package edu.chalmers.sikkr.backend;

/**
 * Interface representing a contact.
 */
public interface Contact {

    public String getFirstName();

    public String getLastName();

    public String getFullName();

    /**
     * Return a list of all the contact's phone numbers.
     */
    public List<String> getPhoneNumbers();

}
