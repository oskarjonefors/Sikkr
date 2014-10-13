package edu.chalmers.sikkr.backend.util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to calculate the priority of a contact.
 */
public class ContactPriorityUtility {

    /**
     * Get a long value that determines the priority of the contact for shows in contact views.
     * Higher is more prioritized, and the value is calculated from the three given parameters.
     * Value will never be negative.
     *
     * @param isFavorite        - Whether or not the contact is marked as favorite.
     * @param timesContacted    - How many times the contact has been contacted.
     * @param lastContacted     - The long time in milliseconds for when the contact was last contacted.
     * @return                  - A long priority number. Higher number means more prioritized.
     */
    public static long getPriority(boolean isFavorite, int timesContacted, long lastContacted) {
        long favoriteNbr = (isFavorite ? 2 : 1);
        long lastContactedNbr = Calendar.getInstance().getTimeInMillis() - lastContacted;

        if (lastContactedNbr < 0) {
            throw new IllegalArgumentException("Last contacted time cannot be later than current time!");
        }

        long priority = favoriteNbr*timesContacted - TimeUnit.MILLISECONDS.toDays(lastContactedNbr)/5;

        return (priority < 0 ? 0 : priority);
    }

}
