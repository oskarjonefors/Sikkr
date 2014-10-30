package edu.chalmers.sikkr.backend.util;
import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import edu.chalmers.sikkr.R;

/**
 * Created by Armand on 2014-10-14.
 *
 * Takes in  a calldate in Milliseconds and through the method callDatetoString returns the time difference between right now and the time of the phonecall.
 */
public final class DateDiffUtility {

    private DateDiffUtility() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }

    public static String callDateToString(long callDateMillis, Context context) {
        Calendar rightNow = GregorianCalendar.getInstance();

        long rightNowMillis = rightNow.getTimeInMillis();

        long deltaMillis = rightNowMillis - callDateMillis;

        int timeDays = (int) TimeUnit.MILLISECONDS.toDays(deltaMillis);
        int timeHours = (int) TimeUnit.MILLISECONDS.toHours(deltaMillis);
        int timeMinutes= (int) TimeUnit.MILLISECONDS.toMinutes(deltaMillis);

        if ((timeDays /7 ) >= 1) {

            if ((timeDays/7) <= 4) {
                return (timeDays/7) + " " + context.getString(R.string.weeks_abbrev);
            }

        } else if (timeDays >= 1) {

            return (timeDays / 1) + " " + context.getString(R.string.days_abbrev);

        } else if ((timeHours / 1) > 0) {

            return timeHours + " " + context.getString(R.string.hours_abbrev);

        } else if ((timeMinutes / 5) >= 1) {

            return timeMinutes + "  " + context.getString(R.string.minutes_abbrev);

        } else if ((timeMinutes / 5 ) < 1) {

            return context.getString(R.string.just_now);
        }

        return context.getString(R.string.earlier);
    }
}
