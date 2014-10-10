package edu.chalmers.sikkr.backend.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox {
    private Context context;
    final private static TheInbox box = new TheInbox();
    private static ArrayList<SmsConversation> smsList;
    final private Map<String, SmsConversation> map = new TreeMap<String, SmsConversation>();

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
        smsList = new ArrayList<SmsConversation>();
        Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);

        while(cursor.moveToNext()) {

            SmsConversation conversation;
            OneSms sms;

            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String person = cursor.getString(cursor.getColumnIndexOrThrow("person"));
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            //If a sms conversation form this contact does not exist, create a new SmsConversation
            if(!map.containsKey(address)) {
                //Toast.makeText(context, "Creating new conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                conversation = new SmsConversation(address, person, date);
                smsList.add(conversation);
                map.put(address, conversation);
            } else {
                //Toast.makeText(context, "Found existing conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                conversation = map.get(address);
            }


            sms = new OneSms(msg, address, date);
            conversation.addSms(sms);


        }

        cursor.close();
    }

    public ArrayList<SmsConversation> getSmsInbox() {
        collectSms();
        return smsList;
    }
}
