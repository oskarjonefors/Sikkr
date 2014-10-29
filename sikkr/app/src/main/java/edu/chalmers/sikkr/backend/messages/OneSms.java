package edu.chalmers.sikkr.backend.messages;

import java.util.Calendar;
import java.util.Date;

import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms extends AbstractMessage {
    private String message;
    private Calendar calendar;
    private boolean isSent;
    private boolean isRead;

    public OneSms(String msg, String date, boolean isSent) {
        this.message = msg;
        this.isSent = isSent;
        isRead = true;
        this.calendar = Calendar.getInstance();

        Date thisDate = new Date(Long.parseLong(date));
        calendar.setTime(thisDate);
    }

    public String getMessage() {
        return message;
    }

    public boolean isSent(){
        return isSent;
    }
    public boolean isRead(){
        return isRead;
    }
    public void markAsRead(){
        isRead = true;
    }
    public void markAsUnread(){
        isRead = false;
    }

    public void play() {
        TextToSpeechUtility.readAloud(message);
    }

    @Override
    public void play(PlaybackListener listener) {
        TextToSpeechUtility.readAloud(message, listener);
    }

    @Override
    public int hashCode() {
        return 11*message.hashCode()*calendar.hashCode() + (isSent ? 1 : 0);
    }

    @Override
    public boolean equals(Object rhs){
        if(this == rhs){
            return true;
        }else if(rhs == null || rhs.getClass()!= OneSms.class ){
            return false;
        }else{
            OneSms tmp = (OneSms)rhs;
            return message.equals(tmp.message) && isSent == tmp.isSent() && calendar.equals(tmp.calendar);
        }
    }

    public Calendar getTimestamp(){
        return calendar;
    }
}
