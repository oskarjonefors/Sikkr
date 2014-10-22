package edu.chalmers.sikkr.backend.util;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Armand on 2014-10-14.
 *
 * Takes in  a calldate in Milliseconds and through the method callDatetoString returns the time difference between right now and the time of the phonecall.
 */
public final class DateDiffUtility {

    private DateDiffUtility() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }

    public static String callDateToString(long callDateMillis) {
        Calendar rightNow = GregorianCalendar.getInstance();

        long rightNowMillis = rightNow.getTimeInMillis();

        long deltaMillis = rightNowMillis - callDateMillis;

        int timeDays = (int) TimeUnit.MILLISECONDS.toDays(deltaMillis);
        int timeHours = (int) TimeUnit.MILLISECONDS.toHours(deltaMillis);
        int timeMinutes= (int) TimeUnit.MILLISECONDS.toMinutes(deltaMillis);

        if ((timeDays /7 ) >= 1) {

            if ((timeDays/7) <= 4) {
                return (timeDays/7) + " w";
            }

        } else if (timeDays >= 1) {

            return (timeDays / 1) + " d";

        } else if ((timeHours / 1) > 0) {

            return timeHours + " h";

        } else if ((timeMinutes / 5) >= 1) {

            return timeMinutes + " min";

        } else if ((timeMinutes / 5 ) < 1) {

            return "Just Now";
        }

        return "Long ago";
    }
}
