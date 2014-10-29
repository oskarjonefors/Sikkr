package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import edu.chalmers.sikkr.backend.MessageNotSentException;
import edu.chalmers.sikkr.backend.messages.InboxDoneLoadingListener;
import edu.chalmers.sikkr.backend.messages.TheInbox;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;

/**
 * A class for sending voice messages as MMS messages. Utilizes the android-smsmms library:
 * https://github.com/klinker41/android-smsmms
 *
 * @author Oskar Jönefors
 */

public class VoiceMessageSender {

    private final String TAG = "VoiceMessageSender";
    private final static VoiceMessageSender singleton = new VoiceMessageSender();
    private Context context;
    private Settings mmsSettings;
    private final List<InboxDoneLoadingListener> listeners;

    private VoiceMessageSender() {
        listeners = new ArrayList<>(2);
    }

    private void setup(Context context) {
        if (context == null) {
            throw new InvalidParameterException("Cannot create instance with null as context");
        }
        this.context = context;

        /* Configure MMS settings - hard-coded for now */
        mmsSettings = new Settings();
        mmsSettings.setMmsc("http://mmsc.tele2.se");
        mmsSettings.setProxy("130.244.202.30");
        mmsSettings.setPort("8080");
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        final int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static VoiceMessageSender getSharedInstance() {
        if(singleton.context == null) {
            throw new UnsupportedOperationException("Context must be supplied through the method" +
                    "setupSingleton(Context context) before an instance can be returned.");
        }
        return singleton;
    }

    public static void setupSingleton(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }
        singleton.setup(context);
    }

    public void sendMessage(final VoiceMessage vmsg, final String receiverNbr) throws IllegalArgumentException {
        Toast.makeText(context, "Preparing to send message", Toast.LENGTH_SHORT).show();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (ServerInterface.serverHasClient(MessageUtils.fixNumber(receiverNbr))) {
                        LogUtility.toastInActivityThread((Activity) context, "Sending voice message via server", Toast.LENGTH_SHORT);
                        ServerInterface.sendVoiceMessage(MessageUtils.fixNumber(receiverNbr), vmsg);
                    } else {
                        LogUtility.toastInActivityThread((Activity) context, "Sending voice message via mms", Toast.LENGTH_SHORT);
                        sendMmsMessage(vmsg, receiverNbr);
                        VoiceMessageFileUtility.saveVoiceMessage(vmsg, MessageUtils.fixNumber(receiverNbr));
                        TheInbox.getInstance().loadInbox(listeners.toArray(new InboxDoneLoadingListener[listeners.size()]));
                    }
                } catch (MessageNotSentException e) {
                    LogUtility.toastInActivityThread((Activity) context, "Could not send voice message", Toast.LENGTH_SHORT);
                    LogUtility.writeLogFile(TAG, e, context);
                }
            }
        };
        try {
            Thread t = new Thread(runnable, "MessageSenderThread");
            t.start();
        } catch (Throwable t) {
            LogUtility.writeLogFile(TAG, t, context);
        }
    }

    /**
     * Send the given VoiceMessage to the given number.
     * @param vmsg - The VoiceMessage to send.
     * @param receiverNbr - The number to send the voice message to.
     * @throws MessageNotSentException - If the message could not be sent.
     * @throws IllegalArgumentException - If the receiver is not a valid one.
     */
    public void sendMmsMessage(VoiceMessage vmsg, String receiverNbr)
            throws MessageNotSentException, IllegalArgumentException {
        final Transaction sendTransaction = new Transaction(context, mmsSettings);
        final Message msg = new Message("Sikkr message", MessageUtils.fixNumber(receiverNbr));

        if(MessageUtils.fixNumber(receiverNbr).length() < 3) {
            throw new IllegalArgumentException("Message receiver number " + receiverNbr + " is invalid.");
        }

        try {
            final InputStream stream = context.getContentResolver().openInputStream(vmsg.getFileUri());
            msg.setMedia(getBytes(stream), "audio/3gp");
            msg.setType(Message.TYPE_SMSMMS);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Audio file " + vmsg.getFileUri() + " not found.");
            throw new MessageNotSentException("Audio file not found.");
        } catch (IOException e) {
            Log.e(TAG, "Couldn't read audio file for sending mms.");
            throw new MessageNotSentException("Audio file could not be read.");
        }
        Log.d(TAG, "Sending message to " + msg.getAddresses()[0] + " with thread id " +
                MessageUtils.getMessageThreadIdByContactId(context, receiverNbr));
        sendTransaction.sendNewMessage(msg, MessageUtils.getMessageThreadIdByContactId(context, receiverNbr));
    }

    public static void addInboxDoneLoadingListener(InboxDoneLoadingListener listener) {
        singleton.listeners.add(listener);
    }
}
