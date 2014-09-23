package edu.chalmers.sikkr.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oskar Jönefors
 */
public class SikkrContact implements Contact {

    private String name;
    private List<String> phoneNumbers;

    public SikkrContact(String name, List<String> phoneNumbers) {
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }
}
