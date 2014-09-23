package edu.chalmers.sikkr.backend;

import java.util.List;

/**
 * @author Oskar Jönefors
 */
public interface ContactBook {

    /**
     * Get a list of all the contacts' initial letters.
     */
    List<Character> getInitialLetters();

    /**
     * Get a list of all the contacts in the contact book.
     */
    List<Contact> getContacts();

    /**
     * Get all contacts starting with the given initial letter.
     * @param initialLetter a char ranging from 'a' to 'z' and also 'å' 'ä' or 'ö'
     * @return
     */
    List<Contact> getContacts(char initialLetter);

}
