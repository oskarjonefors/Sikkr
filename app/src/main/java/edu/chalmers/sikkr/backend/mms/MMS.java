package edu.chalmers.sikkr.backend.mms;

import java.util.Calendar;

import edu.chalmers.sikkr.backend.VoiceMessage;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMS implements VoiceMessage, Comparable<MMS> {


    private final Calendar timestamp;
    private final String sender;
    private final String filePath;

    public MMS(final Calendar timestamp, final String sender, final String filePath) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.filePath = filePath;
    }


    @Override
    public Calendar getTimestamp() {
        return timestamp;
    }

    @Override
    public String getFilePath() {
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
