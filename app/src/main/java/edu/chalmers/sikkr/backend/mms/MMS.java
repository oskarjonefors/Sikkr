package edu.chalmers.sikkr.backend.mms;

import java.util.Date;

import edu.chalmers.sikkr.backend.VoiceMessage;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMS implements VoiceMessage, Comparable<MMS> {


    private final Date date;
    private final String sender;
    private final int filePath;

    public MMS(final Date date, final String sender, final int filePath) {
        this.date = date;
        this.sender = sender;
        this.filePath = filePath;
    }


    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public int getFilePath() {
        return filePath;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public int compareTo(MMS another) {
        return 0;
    }
}
