package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

import java.util.Calendar;

import edu.chalmers.sikkr.backend.AbstractMessage;
import edu.chalmers.sikkr.backend.ListableMessage;
import edu.chalmers.sikkr.backend.VoiceMessage;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMS extends AbstractMessage implements VoiceMessage {


    private final Calendar timestamp;
    private final String sender;
    private final Uri part;

    public MMS(final Calendar timestamp, final String sender, final Uri part) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.part = part;
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
    public String getSender() {
        return sender;
    }

    @Override
    public String getMessage(){
        return "";
    }
    @Override
    public void play(){
    }
    @Override
    public boolean isSent(){
        return true;
    }
    @Override
    public boolean isRead(){
        return true;
    }

}
