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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ProgressListenable;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.ProgressListener;
import edu.chalmers.sikkr.backend.util.ServerInterface;

/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox implements ProgressListenable {
    private Context context;
    private final static TheInbox box = new TheInbox();
    private static List<Conversation> messageList;
    private final Map<String, Conversation> map;
    private final Collection<ProgressListener> listeners;
    private final InboxLoader loader;

    private TheInbox() {
        map = new TreeMap<>();
        listeners = new ArrayList<>();
        loader = new InboxLoader();

        addProgressListener(loader);
        ServerInterface.addSingletonProgressListener(loader);
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
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/inbox"), null, null, null, null);
        int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * 8D);
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String partID = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                MMS mms;
                Conversation conversation;
                Uri partURI;
                Calendar timestamp;
                Cursor curPart = context.getContentResolver().query(Uri.parse("content://mms/" + partID + "/part"), null, null, null, null);

                curPart.moveToFirst();
                partURI = Uri.parse("content://mms/part/" + curPart.getString(0));
                timestamp = new GregorianCalendar();
                timestamp.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow("date")));

                if (!map.containsKey(address)) {
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    conversation = map.get(address);
                }

                mms = new MMS(timestamp, address, partURI, false);
                conversation.addMessage(mms);
                curPart.close();
                notifyListeners(step, "Collecting MMS");
            }
        } else {
            notifyListeners(1D/8D, "Collecting MMS");
        }
        cursor.close();
    }

    private void collectSentMMS() {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/sent"), null, null, null, null);
        int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (cursor.getCount() * 8D);
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
                timestamp = new GregorianCalendar();
                timestamp.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow("date")));

                if (!map.containsKey(address)) {
                    conversation = new Conversation(address);
                    messageList.add(conversation);
                    map.put(address, conversation);
                } else {
                    conversation = map.get(address);
                }

                mms = new MMS(timestamp, address, partURI, true);
                conversation.addMessage(mms);
                curPart.close();
                notifyListeners(step, "Collecting sent MMS");
            }
        } else {
            notifyListeners(1D/8D, "Collecting sent MMS");
        }
        cursor.close();
    }

    private void collectSms() {
        Uri uriToAndroidInbox = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);
        int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * 8D);
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
                sms = new OneSms(msg, address, date, false);
                conversation.addMessage(sms);
                notifyListeners(step, "Collecting SMS");
            }
        } else {
            notifyListeners(1D/8D, "Collecting SMS");
        }

        cursor.close();
    }

    private void collectSentSms() {
        Uri uriToAndroidSentMessages = Uri.parse("content://sms/sent");
        Cursor cursor = context.getContentResolver().query(uriToAndroidSentMessages, null, null, null, null);
        int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * 8D);
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
                sms = new OneSms(msg, address, date, true);
                conversation.addMessage(sms);
                notifyListeners(step, "Collecting sent SMS");
            }
        } else {
            notifyListeners(1D/8D, "Collecting sent SMS");

        }
        cursor.close();

    }

    private void collectWebMessages() throws Exception {
        List<Message> messages = ServerInterface.getReceivedMessages();
        if (!messages.isEmpty()) {
            double step = 1D / (messages.size() * 8D);
            for (Message msg : messages) {
                Conversation conversation;
                if (!map.containsKey(msg.getSender())) {
                    conversation = new Conversation(msg.getSender());
                    map.put(msg.getSender(), conversation);
                    messageList.add(conversation);
                } else {
                    conversation = map.get(msg.getSender());
                }
                conversation.addMessage(msg);
                notifyListeners(step, "Preparing web messages");
            }
        } else {
            notifyListeners(1D/8D, "Preparing web messages");
        }
    }

    private void collectSentWebMessages() throws Exception {
        List<Message> messages = ServerInterface.getSentMessages();
        if (!messages.isEmpty()) {
            double step = 1D / (messages.size() * 8D);
            for (Message msg : messages) {
                Conversation conversation;
                if (!map.containsKey(msg.getSender())) {
                    conversation = new Conversation(msg.getReceiver());
                    map.put(msg.getReceiver(), conversation);
                    messageList.add(conversation);
                } else {
                    conversation = map.get(msg.getReceiver());
                }
                conversation.addMessage(msg);
                notifyListeners(step, "Preparing sent web messages");
            }
        } else {
            notifyListeners(1D/8D, "Preparing sent web messages");
        }
    }

    public void loadInbox(InboxDoneLoadingListener listener) {
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
                collectMMS();
                collectSentMMS();
                collectSms();
                collectSentSms();
                collectWebMessages();
                collectSentWebMessages();
            } catch (Exception e ) {
                e.printStackTrace();
                StackTraceElement[] trace = e.getStackTrace();
                String[] stacktrace = new String[trace.length + 1];
                stacktrace[0] = e.getLocalizedMessage();
                for (int i = 1; i < stacktrace.length + 1; i++) {
                    stacktrace[i] = trace[i].toString();
                }
                LogUtility.writeLogFile("load_inbox_throws", true, stacktrace);
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
