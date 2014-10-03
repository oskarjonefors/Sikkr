package edu.chalmers.sikkr.backend.sms;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms {
    private String message;
    private String senderNbr;
    private String date;

    public OneSms(String msg, String senderNbr, String date) {
        this.message = message;
        this.senderNbr = senderNbr;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }
    public String getDate() {
        return date;
    }
    public String getNbr() {
        return senderNbr;
    }



}
