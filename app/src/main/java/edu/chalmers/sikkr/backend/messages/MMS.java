package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

import java.util.Calendar;

import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMS extends AbstractMessage implements VoiceMessage {


    private final Calendar timestamp;
    private final Uri part;
    private final boolean sent;

    public MMS(final Calendar timestamp, final Uri part, final boolean sent) {
        this.timestamp = timestamp;
        this.part = part;
        this.sent = sent;
    }


    @Override
    public Calendar getTimestamp() {
        return timestamp;
    }

    @Override
    public Uri getFileUri() {
        return part;
    }

    @Override
    public void play(){
        VoiceMessagePlayer.getSharedInstance().playMessage(this);
    }

    @Override
    public boolean isSent(){
        return sent;
    }
    @Override
    public boolean isRead(){
        return true;
    }

    @Override
    public void markAsRead() {

    }

}
