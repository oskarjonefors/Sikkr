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
import edu.chalmers.sikkr.backend.util.MessageUtils;
import edu.chalmers.sikkr.backend.util.ProgressListener;
import edu.chalmers.sikkr.backend.util.ServerInterface;
import edu.chalmers.sikkr.backend.util.VoiceMessageFileUtility;

/**
 * Created by Jingis on 2014-09-27.
 */

public class TheInbox implements ProgressListenable {
    private final static double numberOfOperations = 5D;


    private static TheInbox box;

    private final Context context;
    private final List<Conversation> messageList;
    private final Map<String, Conversation> map;
    private final Collection<ProgressListener> listeners;

    private InboxLoader loader;

    private TheInbox(Context context) {
        map = new TreeMap<>();
        listeners = new ArrayList<>();
        messageList = new ArrayList<>();
        this.context = context;

    }

    public static void setupInbox(Context context) {
        if (context != null) {
            box = new TheInbox(context);
        }
    }

    public static TheInbox getInstance() {
        return box;
    }

    private void collectSms() {
        final Uri uriToAndroidInbox = Uri.parse("content://sms/inbox");
        final Cursor cursor = context.getContentResolver().query(uriToAndroidInbox, null, null, null, null);
        final int count = cursor.getCount();
        if (count != 0) {
            double step = 1D / (count * numberOfOperations);
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                Conversation conversation = getConversation(address);
                OneSms sms;

                //If a sms conversation form this contact does not exist, create a new Conversation
                if (conversation == null) {
                    conversation = new Conversation(address, false);
                    messageList.add(conversation);
                    map.put(address, conversation);
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
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                Conversation conversation = getConversation(address);
                OneSms sms;

                if (conversation == null) {
                    conversation = new Conversation(address, false);
                    messageList.add(conversation);
                    map.put(address, conversation);
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
        LogUtility.writeLogFile("TheInbox", "Collecting messages from the server and saving them");
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
        LogUtility.writeLogFile("TheInbox", "Collecting messages from the device");
        List<Message> messages = VoiceMessageFileUtility.readMessages(context);
        if (!messages.isEmpty()) {
            double step = 1D / (messages.size() * numberOfOperations);
            for (Message msg : messages) {
                String address = msg.isSent() ? msg.getReceiver() : msg.getSender();
                Conversation conversation = getConversation(address);
                if (conversation == null) {
                    conversation = new Conversation(address, true);
                    messageList.add(conversation);
                    map.put(address, conversation);
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
        for (Conversation conv : messageList) {
            if (conv.hasLocalNumber() && conv.getAddress().equals(address)) {
                return conv;
            } else if (conv.getFixedNumber().equals(MessageUtils.fixNumber(address))) {
                return conv;
            }
        }
        return null;
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
                collectLocalMessages();
                if (listener != null) {
                    collectSms();
                    collectSentSms();
                }
            } catch (Exception e ) {
                LogUtility.writeLogFile("load_inbox_throws", e, context);
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
            if (listener != null ) {listener.onDone(); }
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
