package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.util.Log;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import edu.chalmers.sikkr.backend.MessageNotSentException;
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

    private VoiceMessageSender() {}

    private void setup(Context context) {
        this.context = context;

        /* Configure MMS settings - hard-coded for now */
        mmsSettings = new Settings();
        mmsSettings.setMmsc("http://mmsc.tele2.se");
        mmsSettings.setProxy("130.244.202.30");
        mmsSettings.setPort("8080");
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
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

    public void sendMessage(VoiceMessage vmsg, String receiverNbr)
            throws MessageNotSentException, IllegalArgumentException {
        if (ServerInterface.serverHasClient(receiverNbr)) {
            ServerInterface.sendVoiceMessage(receiverNbr, vmsg);
        } else {
            sendMmsMessage(vmsg, receiverNbr);
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
}
