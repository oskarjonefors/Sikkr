package edu.chalmers.sikkr.backend;

/**
 * Created by ivaldi on 2014-10-02.
 */
public class MessageNotSentException extends Exception {

    public MessageNotSentException(String s) {
        super(s);
    }
}
