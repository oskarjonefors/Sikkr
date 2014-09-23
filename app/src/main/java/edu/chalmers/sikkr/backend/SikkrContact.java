package edu.chalmers.sikkr.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oskar Jönefors
 */
public class SikkrContact implements Contact {

    final private String name;
    final private List<String> phoneNumbers;
    final private List<String> mobilePhoneNumbers;



    public SikkrContact(String name) {
        this.name = name;
        phoneNumbers = new ArrayList<String>();
        mobilePhoneNumbers = new ArrayList<String>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void addPhoneNumber(String number) {
        phoneNumbers.add(number);
    }

    public void addMobilePhoneNumber(String number) {
        addPhoneNumber(number);
        mobilePhoneNumbers.add(number);
    }

    @Override
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    @Override
    public List<String> getMobilePhoneNumbers() {
        return mobilePhoneNumbers;
    }

}
