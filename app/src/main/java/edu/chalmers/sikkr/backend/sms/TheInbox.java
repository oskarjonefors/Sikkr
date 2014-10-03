package edu.chalmers.sikkr.backend.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox {
    private Context context;
    final private static TheInbox box = new TheInbox();
    private static ArrayList<OneSms> smsList;

    private TheInbox() {}

    public static void setupInbox(Context context) {
        box.setUp(context);
    }

    private void setUp(Context context) {
        this.context = context;
    }

    public static TheInbox getInstance() {
        return box;
    }

    private void collectSms() {

        Uri uriToAndroidInbox = Uri.parse("content://sms/inbox");
        smsList = new ArrayList<OneSms>();

        Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);

        cursor.moveToNext();
        String msg = cursor.getString(cursor.getColumnIndexOrThrow("person"));

        cursor.close();
    }

    public ArrayList<OneSms> getSmsInbox() {
        collectSms();
        return null;
    }
}
