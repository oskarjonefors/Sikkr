package edu.chalmers.sikkr.backend.calls;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

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

        callList = new ArrayList<OneCall>();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");
        Cursor cursor = context.getContentResolver().query(callUri, null, null, null, strOrder);

        while (cursor.moveToNext()) {
            OneCall call = new OneCall();
            call.setCallNumber(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)));
            call.setCallDate(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)));
            call.setCallType(cursor.getInt(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)));
            call.setIsCallNew(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW)));
            call.setContactID(getContactIDFromNumber(call.getCallNumber()));
            Log.d("CallLog", "Get ContactID " + call.getContactID());

            if(!isCallOld(call.getCallDate())) {
                callList.add(call);
            }
        }
        cursor.close();
    }
    private String getContactIDFromNumber(String contactNbr){

        contactNbr= Uri.encode(contactNbr);
        String phoneContactID;
        String[] projection =new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
        Cursor contactCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,contactNbr),projection,null,null,null);

        if (contactCursor.moveToFirst()){
            phoneContactID = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            contactCursor.close();
            return phoneContactID;
        }
        else { return null; }
    }

    private boolean isCallOld(String callDate) {
        Calendar rightNow = GregorianCalendar.getInstance();
        long rightNowMillis = rightNow.getTimeInMillis();
        long deltaMillis = rightNowMillis - Long.parseLong(callDate);
        return TimeUnit.MILLISECONDS.toDays(deltaMillis) / 7 > 4;
    }


    public ArrayList<OneCall> getCallList() {
        collectCallLog();
        return callList;
    }
}
