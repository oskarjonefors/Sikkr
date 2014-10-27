package edu.chalmers.sikkr.backend.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Simple utility class for retrieving message thread ID:s and cleaning up phone numbers.
 * @author Oskar Jönefors
 */
public class MessageUtils {

    public final static String TAG = "MessageUtils";

    /**
     * If there is an existing message thread with the given number, return it.
     * If there isn't, "0" will be returned.
     * @param phoneNumber
     * @return - A long thread ID.
     */
    public static long getMessageThreadIdByContactId(Context context, String phoneNumber) {

        final ContentResolver cr = context.getContentResolver();

        Uri threadIdUri = Uri.parse("content://mms-sms/threadID");
        Uri.Builder builder = threadIdUri.buildUpon();
        builder.appendQueryParameter("recipient", phoneNumber);
        Uri appendedUri = builder.build();

        long threadId = 0;
        final Cursor idCursor = cr.query(appendedUri, new String[]{"_id"}, null, null, null);

        if(idCursor != null) {
            try {
                if(idCursor.moveToFirst()) {
                    threadId = idCursor.getLong(0);
                }
            } finally {
                idCursor.close();
            }
        }

        return threadId;
    }

    public static String fixNumber(String number) {
        number = number.replaceAll("[^0-9]", "");
        if (number.startsWith("46")) {
            number = "0" + number.substring(2);
        }
        return number;
    }
}
