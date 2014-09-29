package edu.chalmers.sikkr.backend;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oskar Jönefors
 */
public class SikkrContact implements Contact {

    final private String name;
    final private String id;
    final private List<String> phoneNumbers;
    final private List<String> mobilePhoneNumbers;
    final private Bitmap photo;


    public SikkrContact(final String name, final String id, final Bitmap photo) {
        this.name = name;
        this.id = id;
        this.photo = photo;
        phoneNumbers = new ArrayList<String>();
        mobilePhoneNumbers = new ArrayList<String>();
    }

    public SikkrContact(final String name, final String id) {
        this(name, id, null);
    }

    @Override
    public Bitmap getPhoto() {
        return photo;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void addPhoneNumber(String number) {
        phoneNumbers.add(number);
        Log.d("SikkrContact ", "Added phone number " + number + " for " + name);
    }

    public void addMobilePhoneNumber(String number) {
        addPhoneNumber(number);
        mobilePhoneNumbers.add(number);
        Log.d("SikkrContact ", "Added phone number " + number + " for " + name);
    }

    @Override
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    @Override
    public List<String> getMobilePhoneNumbers() {
        return mobilePhoneNumbers;
    }

    @Override
    public int compareTo(Contact another) {
        int compare = name.compareTo(another.getName());
        return compare == 0 ? id.compareTo(another.getID()) : compare;
    }
}
