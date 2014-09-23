package edu.chalmers.sikkr.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oskar Jönefors
 */
public class SikkrContact implements Contact {

    private String firstName;
    private String lastName;
    private List<String> phoneNumbers;

    public SikkrContact(String firstName, String lastName, List<String> phoneNumbers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }
}
