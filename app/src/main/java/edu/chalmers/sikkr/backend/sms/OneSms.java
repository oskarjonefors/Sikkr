package edu.chalmers.sikkr.backend.sms;

/**
 * Created by Jingis on 2014-09-30.
 */
public class OneSms {
    private String message;
    private int senderNbr;

    public void setMessage(String message) {
        this.message = message;
    }
    public int getNbr() {
        return senderNbr;
    }
    public String getMessage() {
        return message;
    }
    public void setSenderNbr(int nbr) {
        senderNbr = nbr;
    }
}
