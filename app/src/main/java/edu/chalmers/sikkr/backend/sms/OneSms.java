package edu.chalmers.sikkr.backend.sms;

import java.util.Calendar;
import java.util.Date;

import edu.chalmers.sikkr.backend.AbstractMessage;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms extends AbstractMessage {
    private String message;
    private String senderNbr;
    private String date;
    private Calendar calendar;
    private boolean isSent;
    private boolean isRead;

    public OneSms(String msg, String senderNbr, String date, boolean isSent) {
        this.message = msg;
        this.senderNbr = senderNbr;
        this.date = date;
        this.isSent = isSent;
        isRead = false;
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
    public String getDate() {
        return date;
    }
    public String getSender() {
        return senderNbr;
    }

    public void play() {
        TextToSpeechUtility.readAloud(message);
    }

    @Override
    public int hashCode() {
        return 11*message.hashCode()*senderNbr.hashCode()*date.hashCode() + (isSent ? 1 : 0);
    }

    @Override
    public boolean equals(Object rhs){
        if(this == rhs){
            return true;
        }else if(rhs == null || rhs.getClass()!= OneSms.class ){
            return false;
        }else{
            OneSms tmp = (OneSms)rhs;
            return message.equals(tmp.message) && senderNbr.equals(tmp.senderNbr) && date.equals(tmp.date) && isSent == tmp.isSent();
        }
    }

    public Calendar getTimestamp(){
        Date thisDate = new Date(Long.parseLong(date));
        calendar = Calendar.getInstance();
        calendar.setTime(thisDate);
        return calendar;
    }
}
