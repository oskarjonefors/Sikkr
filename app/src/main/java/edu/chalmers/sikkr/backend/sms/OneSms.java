package edu.chalmers.sikkr.backend.sms;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms {
    private String message;
    private String senderNbr;
    private String date;


    public void setMessage(String message) {
        this.message = message;
    }
    public void setSenderNbr(String nbr) {
        senderNbr = nbr;
    }
    public void setDate(String date) {
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
