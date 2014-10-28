package edu.chalmers.sikkr.backend.messages;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ProgressListenable;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.ProgressListener;
import edu.chalmers.sikkr.backend.util.ServerInterface;
import edu.chalmers.sikkr.backend.util.VoiceMessageFileUtility;

/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox implements ProgressListenable {
    private final static TheInbox box = new TheInbox();
    private final static double numberOfOperations = 10D;

    private Context context;
    private static List<Conversation> messageList;
    private final Map<String, Conversation> map;
    private final Collection<ProgressListener> listeners;
    private InboxLoader loader;

    private TheInbox() {
        map = new TreeMap<>();
        listeners = new ArrayList<>();

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
        final Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/inbox"), null, null, null, null);
        final int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * numberOfOperations);
            while (cursor.moveToNext()) {
                String partID = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                Cursor curPart = context.getContentResolver().query(Uri.parse("content://mms/" + partID + "/part"), null, null, null, null);
                curPart.moveToFirst();
                if (curPart.getColumnCount() >= 3 && curPart.getString(3).startsWith("audio")) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    //context.getContentResolver().
                    Uri partURI = Uri.parse("content://mms/part/" + curPart.getString(0));
                    MMS mms;
                    Conversation conversation;
                    Calendar timestamp;

                    Date thisDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("date"))));
                    timestamp = Calendar.getInstance();
                    timestamp.setTime(thisDate);

                    if (!map.containsKey(address)) {
                        conversation = new Conversation(address);
                        messageList.add(conversation);
                        map.put(address, conversation);
                    } else {
                        conversation = map.get(address);
                    }

                    mms = new MMS(timestamp, partURI, false, context);
                    conversation.addMessage(mms);
                    curPart.close();
                }
                notifyListeners(step, "Collecting MMS");
            }
        } else {
            notifyListeners(1D/numberOfOperations, "Collecting MMS");
        }
        cursor.close();
    }

    private void collectSentMMS() {
        final Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/sent"), null, null, null, null);
        final int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (cursor.getCount() * numberOfOperations);
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String partID = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                MMS mms;
                Conversation conversation;
                Uri partURI;
                Calendar timestamp;
                Cursor curPart = context.getContentResolver().query(Uri.parse ("content://mms/" + partID + "/part"), null, null, null, null);

                curPart.moveToFirst();
                partURI = Uri.parse("content://mms/part/" + curPart.getString(0));

                Date thisDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("date"))));
                timestamp = Calendar.getInstance();
                timestamp.setTime(thisDate);

                if (!map.containsKey(address)) {
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    conversation = map.get(address);
                }

                mms = new MMS(timestamp, partURI, true, context);
                conversation.addMessage(mms);
                curPart.close();
                notifyListeners(step, "Collecting sent MMS");
            }
        } else {
            notifyListeners(1D/numberOfOperations, "Collecting sent MMS");
        }
        cursor.close();
    }

    private void collectSms() {
        final Uri uriToAndroidInbox = Uri.parse("content://sms/inbox");
        final Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);
        final int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * numberOfOperations);
            while (cursor.moveToNext()) {

                Conversation conversation;
                OneSms sms;

                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                //If a sms conversation form this contact does not exist, create a new Conversation
                if (!map.containsKey(address)) {
                    //Toast.makeText(context, "Creating new conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    //Toast.makeText(context, "Found existing conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                    conversation = map.get(address);
                }
                sms = new OneSms(msg, date, false);
                conversation.addMessage(sms);
                notifyListeners(step, "Collecting SMS");
            }
        } else {
            notifyListeners(1D/numberOfOperations, "Collecting SMS");
        }

        cursor.close();
    }

    private void collectSentSms() {
        final Uri uriToAndroidSentMessages = Uri.parse("content://sms/sent");
        final Cursor cursor = context.getContentResolver().query(uriToAndroidSentMessages, null, null, null, null);
        final int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * numberOfOperations);
            while (cursor.moveToNext()) {
                Conversation conversation;
                OneSms sms;

                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                if (!map.containsKey(address)) {
                    //Toast.makeText(context, "Creating new conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    //Toast.makeText(context, "Found existing conversation: "+address+"\n"+msg, Toast.LENGTH_SHORT).show();
                    conversation = map.get(address);
                }
                sms = new OneSms(msg, date, true);
                conversation.addMessage(sms);
                notifyListeners(step, "Collecting sent SMS");
            }
        } else {
            notifyListeners(1D/numberOfOperations, "Collecting sent SMS");

        }
        cursor.close();

    }

    private void collectAndSaveServerMessages() throws Exception {
        final List<ServerMessage> messages = ServerInterface.getReceivedMessages();
        if (!messages.isEmpty()) {
            double step = 1D / (messages.size() * numberOfOperations);
            for (ServerMessage msg : messages) {
                VoiceMessageFileUtility.saveServerMessage(context, msg);
                notifyListeners(step, "Preparing web messages");
            }
        } else {
            notifyListeners(1D/numberOfOperations, "Preparing web messages");
        }
    }

    public void collectLocalMessages() {
        List<Message> messages = VoiceMessageFileUtility.readMessages(context);
        if (!messages.isEmpty()) {
            double step = 1D / (messages.size() * numberOfOperations);
            for (Message msg : messages) {
                Conversation conversation;
                String address = msg.isSent() ? msg.getReceiver() : msg.getSender();
                if (!map.containsKey(address)) {
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    conversation = map.get(address);
                }
                conversation.addMessage(msg);
                notifyListeners(step, "Collecting web messages.");
            }
        }
    }

    public void loadInbox(InboxDoneLoadingListener listener) {
        if (loader != null) {
            removeProgressListener(loader);
            ServerInterface.removeSingletonProgressListener(loader);
        }
        loader = new InboxLoader();

        addProgressListener(loader);
        ServerInterface.addSingletonProgressListener(loader);
        loader.execute(listener);
    }

    public List<Conversation> getMessageInbox() {
        return messageList;
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyListeners(double progress, String taskMsg) {
        for (ProgressListener listener : listeners) {
            listener.notifyProgress(progress, "Inbox", taskMsg);
        }
    }

    void updateProgress() {
        ProgressBar initBar = (ProgressBar) ((Activity) context).findViewById(R.id.inboxProgressBar);

        if (initBar != null ) {
            initBar.setProgress((int) (loader.getProgress() * initBar.getMax()));
        }
    }

    public Conversation getConversation(String address) {
        Conversation tmpConv = null;
        for (Conversation conv : messageList) {
            if (conv.getAddress().equals(address)) {
                tmpConv = conv;
            }
        }
        return tmpConv;
    }

    private class InboxLoader extends AsyncTask<InboxDoneLoadingListener, String, Boolean> implements ProgressListener {

        private double progress = 0;
        private InboxDoneLoadingListener listener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(InboxDoneLoadingListener... params) {
            try {
                listener = params[0];
                collectAndSaveServerMessages();
                //collectMMS();
                //collectSentMMS();
                collectSms();
                collectSentSms();
                collectLocalMessages();
            } catch (Exception e ) {
                LogUtility.writeLogFile("load_inbox_throws", e);
                return false;
            }
            return true; //If successful
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.progress += Double.parseDouble(progress[0]);
            updateProgress();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            listener.onDone();
        }

        @Override
        public void notifyProgress(double progress, String senderTag, String taskMsg) {
            publishProgress(progress + "", senderTag, taskMsg);
        }

        public double getProgress() {
            return progress;
        }
    }


}
