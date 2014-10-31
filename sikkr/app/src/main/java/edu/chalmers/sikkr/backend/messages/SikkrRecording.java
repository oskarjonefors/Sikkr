package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

import java.util.Calendar;

import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;

/**
 * Created by Eric on 2014-10-02.
 */
public class SikkrRecording extends AbstractMessage implements VoiceMessage {


    private final Calendar timestamp;
    private final Uri part;

    private boolean read;

    public SikkrRecording(final Calendar timestamp, final Uri part) {
        this.timestamp = timestamp;
        this.part = part;
        this.read = false;
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
    public void play(PlaybackListener listener) {
        VoiceMessagePlayer.getSharedInstance().playMessage(this, listener);
    }

    @Override
    public boolean isSent(){
        return true;
    }

    @Override
    public boolean isRead(){
        return read;
    }

    @Override
    public void markAsRead() {
        read = true;
    }

}
