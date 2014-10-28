package edu.chalmers.sikkr.backend.messages;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMS extends AbstractMessage implements VoiceMessage {


    private final Calendar timestamp;
    private final Uri part;
    private final Context context;
    private final boolean sent;

    private boolean read;

    public MMS(final Calendar timestamp, final Uri part, final boolean sent, final Context context) {
        this.timestamp = timestamp;
        this.part = part;
        this.sent = sent;
        this.read = false;
        this.context = context;
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
        return read;
    }

    @Override
    public void markAsRead() {
        read = true;
    }

}
