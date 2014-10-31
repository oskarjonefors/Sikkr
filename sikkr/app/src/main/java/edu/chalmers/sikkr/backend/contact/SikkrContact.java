package edu.chalmers.sikkr.backend.contact;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import edu.chalmers.sikkr.backend.util.ContactPriorityUtility;

/**
 * @author Oskar Jönefors
 */
public class SikkrContact implements Contact {

    final private String name;
    final private String id;
    final private List<String> phoneNumbers;
    final private List<String> mobilePhoneNumbers;
    final private Bitmap photo;
    private boolean isFavorite;
    private long priority = 0;


    public SikkrContact(final String name, final String id, final Bitmap photo) {
        this.name = name;
        this.id = id;
        this.photo = photo;
        phoneNumbers = new ArrayList<String>();
        mobilePhoneNumbers = new ArrayList<String>();
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

    @Override
    public String getDefaultNumber() {
        if (mobilePhoneNumbers.size() > 0) {
            return mobilePhoneNumbers.get(0);
        } else if (phoneNumbers.size() > 0) {
            return phoneNumbers.get(0);
        } else {
            return null;
        }
    }

    @Override
    public long getPriority() {
        return priority;
    }

    @Override
    public boolean isFavorite() {
        return isFavorite;
    }

    @Override
    public void calculatePriority(boolean isFavorite, int timesContacted, long lastContacted) {
        priority = ContactPriorityUtility.getPriority(isFavorite, timesContacted, lastContacted);
    }

    @Override
    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public int compareTo(Contact another) {
        if (another == null) {
            return 1;
        }
        if (isFavorite() && !another.isFavorite()) {
            return -1;
        }
        if (!isFavorite() && another.isFavorite()) {
            return 1;
        }

        int compare = name.compareTo(another.getName());
        return compare == 0 ? id.compareTo(another.getID()) : compare;
    }
}
