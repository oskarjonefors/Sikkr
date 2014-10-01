package edu.chalmers.sikkr.backend.util;

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
        Log.e("print", (context == null) + "");
        Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);

        if(cursor.moveToFirst()) {
            do {
                OneSms sms = new OneSms();
                sms.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                sms.setSenderNbr(cursor.getInt((cursor.getColumnIndexOrThrow("address"))));

                smsList.add(sms);
            } while (cursor.moveToNext());
        }
    }

    public ArrayList<OneSms> getSmsInbox() {
        collectSms();
        return smsList;
    }
}
