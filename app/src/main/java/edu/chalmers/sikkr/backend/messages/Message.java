package edu.chalmers.sikkr.backend.messages;

import android.content.Context;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;
import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;

/**
 * Created by Eric on 2014-10-19.
 */
public final class Message extends AbstractMessage implements VoiceMessage {

    private final String SENDER, RECEIVER;
    private final byte[] CONTENT;
    private final int TYPE;
    private final Calendar TIMESTAMP;
    private final Uri tmp;
    private final boolean sent;

    public final static int TYPE_TEXT = 1;
    public final static int TYPE_DATA = 2;

    public Message(final Context context, final String SENDER, final String RECEIVER, final byte[] CONTENT,
                    final int TYPE, final long TIMESTAMP, final boolean sent) {
        this.SENDER = SENDER;
        this.RECEIVER = RECEIVER;
        this.CONTENT = CONTENT;
        this.TYPE = TYPE;
        this.TIMESTAMP = new GregorianCalendar();
        this.TIMESTAMP.setTimeInMillis(TIMESTAMP);
        this.sent = sent;
        Uri tmp;
        if (TYPE == TYPE_DATA) {
            final File messageDir = new File(context.getFilesDir(), ".temp/");
            final File messageFile = generateMessageFile(messageDir);
            if (!messageDir.exists()) {
                messageDir.mkdirs();
                messageDir.deleteOnExit();
            }

            try {
                messageFile.createNewFile();
                messageFile.deleteOnExit();
                saveByteDataToFile(messageFile, CONTENT);
                tmp = Uri.fromFile(messageFile);
            } catch (Exception e) {
                tmp = null;
            }
            this.tmp = tmp;
        } else {
            this.tmp = null;
        }
    }

    private static void saveByteDataToFile(File file, byte[] data) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        dos.write(data);
        dos.flush();
        dos.close();
    }

    private File generateMessageFile(File parent) {
        File file;
        do {
            Random random = new SecureRandom();
            random.setSeed((long) (hashCode() * Math.random()));
            file = new File(parent, random.nextLong() + ".tmp");
        } while (file.exists());
        return file;
    }

    public String getReceiver() {
        return RECEIVER;
    }

    @Override
    public Calendar getTimestamp() {
        return TIMESTAMP;
    }

    @Override
    public String getSender() {
        return SENDER;
    }

    @Override
    public void play() {
        switch (TYPE) {
            case TYPE_TEXT:
                TextToSpeechUtility.readAloud(new String(CONTENT));
                break;
            case TYPE_DATA:
                VoiceMessagePlayer.getSharedInstance().playMessage(this);
                break;
        }
    }

    @Override
    public boolean isSent() {
        return sent;
    }

    @Override
    public Uri getFileUri() {
        return tmp;
    }

    @Override
    public int hashCode() {
        return 17 * SENDER.hashCode() + 13 * RECEIVER.hashCode() + 23 * CONTENT.hashCode() +
                11 * TIMESTAMP.hashCode() + 27 * TYPE;
    }
}
