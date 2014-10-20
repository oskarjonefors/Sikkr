package edu.chalmers.sikkr.backend.messages;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.chalmers.sikkr.backend.util.ServerInterface;

import static android.provider.BaseColumns._ID;
import static android.provider.Telephony.BaseMmsColumns.DATE;
import static android.provider.Telephony.Mms.Addr.ADDRESS;
import static android.provider.Telephony.*;

/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox {
    private Context context;
    private final static TheInbox box = new TheInbox();
    private static List<Conversation> messageList;
    private static List<Conversation> sentList;
    private final Map<String, Conversation> map = new TreeMap<>();

    private TheInbox() {

    }

    public static void setupInbox(Context context) {
        box.setUp(context);
    }

    private void setUp(Context context)
    {
        messageList = new ArrayList<>();
        this.context = context;
    }

    public static TheInbox getInstance() {
        return box;
    }

    private void collectMMS() {
        Cursor cursor = context.getContentResolver().query(Mms.Inbox.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
            String person = cursor.getString(cursor.getColumnIndexOrThrow("person"));
            String partID = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
            MMS mms;
            Conversation conversation;
            Uri partURI;
            Calendar timestamp;
            Cursor curPart = context.getContentResolver().query(Uri.parse ("content://mms/" + partID + "/part"), null, null, null, null);

            curPart.moveToFirst();
            partURI = Uri.parse("content://mms/part/" + curPart.getString(0));
            timestamp = new GregorianCalendar();
            timestamp.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(DATE)));

            if (!map.containsKey(address)) {
                conversation = new Conversation(address, person, timestamp.getDisplayName(Calendar.DATE, Calendar.LONG, Locale.getDefault()), false);
                messageList.add(conversation);
                map.put(address, conversation);
            } else {
                conversation = map.get(address);
            }

            mms = new MMS(timestamp, address, partURI, false);
            conversation.addMessage(mms);
            curPart.close();
        }
        cursor.close();
    }

    private void collectSentMMS() {
        Cursor cursor = context.getContentResolver().query(Mms.Sent.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
            String person = cursor.getString(cursor.getColumnIndexOrThrow("person"));
            String partID = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
            MMS mms;
            Conversation conversation;
            Uri partURI;
            Calendar timestamp;
            Cursor curPart = context.getContentResolver().query(Uri.parse ("content://mms/" + partID + "/part"), null, null, null, null);

            curPart.moveToFirst();
            partURI = Uri.parse("content://mms/part/" + curPart.getString(0));
            timestamp = new GregorianCalendar();
            timestamp.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(DATE)));

            if (map.containsKey(address)) {
                conversation = map.get(address);
                mms = new MMS(timestamp, address, partURI, true);
                conversation.addMessage(mms);
            }

            curPart.close();
        }
        cursor.close();
    }

    private void collectSms() {
        Uri uriToAndroidInbox = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);
        while(cursor.moveToNext()) {

            Conversation conversation;
            OneSms sms;

            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String person = cursor.getString(cursor.getColumnIndexOrThrow("person"));
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            //If a sms conversation form this contact does not exist, create a new Conversation
            if(!map.containsKey(address)) {
                //Toast.makeText(context, "Creating new conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                conversation = new Conversation(address, person, date, false);
                messageList.add(conversation);
                map.put(address, conversation);
            } else {
                //Toast.makeText(context, "Found existing conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                conversation = map.get(address);
            }
            sms = new OneSms(msg, address, date, false);
            conversation.addMessage(sms);
        }

        cursor.close();
    }

    private void collectSentSms() {
        Uri uriToAndroidSentMessages = Uri.parse("content://sms/sent");
        Cursor cursor = context.getContentResolver().query(uriToAndroidSentMessages, null, null, null, null);
        while(cursor.moveToNext()){
            Conversation conversation;
            OneSms sms;

            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String person = cursor.getString(cursor.getColumnIndexOrThrow("person"));
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            if(map.containsKey(address)) {
                conversation = map.get(address);
                sms = new OneSms(msg, address, date, true);
                conversation.addMessage(sms);
            }
        }
        cursor.close();

    }

    private void collectWebMessages() {
        List<Message> messages = ServerInterface.getReceivedMessages();
        if (!messages.isEmpty()) {
            for (Message msg : messages) {
                Conversation conversation;
                if (!map.containsKey(msg.getSender())) {
                    conversation = new Conversation(msg.getSender(), "", msg.getTimestamp().getDisplayName(Calendar.DATE, Calendar.LONG, Locale.getDefault()), false);
                    map.put(msg.getSender(), conversation);
                    messageList.add(conversation);
                } else {
                    conversation = map.get(msg.getSender());
                }
                conversation.addMessage(msg);
            }
        }
    }

    private void collectSentWebMessages() {
        List<Message> messages = ServerInterface.getSentMessages();
        if (!messages.isEmpty()) {
            for (Message msg : messages) {
                Conversation conversation;
                if (!map.containsKey(msg.getSender())) {
                    conversation = new Conversation(msg.getReceiver(), "", msg.getTimestamp().getDisplayName(Calendar.DATE, Calendar.LONG, Locale.getDefault()), true);
                    map.put(msg.getReceiver(), conversation);
                    messageList.add(conversation);
                } else {
                    conversation = map.get(msg.getReceiver());
                }
                conversation.addMessage(msg);
            }
        }
    }

    public List<Conversation> getMessageInbox() {
        collectMMS();
        collectSentMMS();
        collectSms();
        collectSentSms();
        collectWebMessages();
        collectSentWebMessages();
        return messageList;
    }


}
