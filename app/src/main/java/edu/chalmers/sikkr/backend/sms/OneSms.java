package edu.chalmers.sikkr.backend.sms;

import java.util.Date;

import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms implements Comparable<OneSms> {
    private String message;
    private String senderNbr;
    private String date;
    private boolean isSent;

    public OneSms(String msg, String senderNbr, String date, boolean isSent) {
        this.message = msg;
        this.senderNbr = senderNbr;
        this.date = date;
        this.isSent = isSent;
    }

    public String getMessage() {
        return message;
    }
    public boolean isSent(){
        return isSent;
    }
    public String getDate() {
        return date;
    }
    public String getNbr() {
        return senderNbr;
    }

    public void play() {
        TextToSpeechUtility.readAloud(message);
    }

    @Override
    public int compareTo(OneSms another) {
        Date thisDate = new Date(Long.parseLong(date));
        Date otherDate = new Date(Long.parseLong(another.date));
        return -thisDate.compareTo(otherDate);

    }

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
}
