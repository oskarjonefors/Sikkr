package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;

/**
 * Created by Eric on 2014-10-19.
 */
public final class Message extends AbstractMessage implements VoiceMessage {

    private final String SENDER, RECEIVER;
    private final Calendar TIMESTAMP;
    private final Uri path;
    private final boolean sent;
    private boolean read = false;

    public Message(final String SENDER, final String RECEIVER, final Uri path,
            final long TIMESTAMP, final boolean sent) {
        this.SENDER = SENDER;
        this.RECEIVER = RECEIVER;
        this.TIMESTAMP = new GregorianCalendar();
        this.TIMESTAMP.setTimeInMillis(TIMESTAMP);
        this.sent = sent;
        this.path = path;
    }

    public String getReceiver() {
        return RECEIVER;
    }

    @Override
    public Calendar getTimestamp() {
        return TIMESTAMP;
    }

    public String getSender() {
        return SENDER;
    }

    @Override
    public void play() {
        VoiceMessagePlayer.getSharedInstance().playMessage(this);
    }

    @Override
    public boolean isSent() {
        //TODO
        return sent;
    }

    @Override
    public boolean isRead() {
        return read;
    }

    @Override
    public void markAsRead() {
        read = true;
    }

    @Override
    public Uri getFileUri() {
        return path;
    }

    @Override
    public int hashCode() {
        return 17 * SENDER.hashCode() + 13 * RECEIVER.hashCode() +
                11 * TIMESTAMP.hashCode() + 3 * path.hashCode();
    }
}
