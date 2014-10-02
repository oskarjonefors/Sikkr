package edu.chalmers.sikkr.backend.calls;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Mia on 02/10/14.
 */
public class CallLog {
    private Context context;
    final private static CallLog callLog = new CallLog();
    private static ArrayList<OneCall> callList;

    private CallLog() {}

    public static void setUpCallLog(Context context) {
        callLog.setUp(context);
    }

    private void setUp (Context context) {
        this.context = context;
    }

    public static CallLog getInstance() {
        return callLog;
    }

    private void collectCallLog(){

        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");
        Cursor cursor = context.getContentResolver().query(callUri, null, null, null, strOrder);

        while (cursor.moveToNext()) {
            OneCall call = new OneCall();
            call.setCallNumber(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)));
            call.setCallName(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)));
            call.setCallDate(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)));
            call.setCallType(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)));
            call.setIsCallNew(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW)));

            callList.add(call);
        }
    }

    public ArrayList getCallList() {
        collectCallLog();
        return callList;
    }
}
