package edu.chalmers.sikkr.backend;

/**
 * @author Oskar Jönefors
 */
public class MessageNotSentException extends Exception {

    public MessageNotSentException(String s) {
        super(s);
    }
}
