package edu.chalmers.sikkr.backend.messages;

import java.util.Calendar;

/**
 * Created by Eric on 2014-10-03.
 */
public interface ListableMessage extends Comparable<ListableMessage> {

    /**
     * @return the Calendar timestamp for when the message was created.
     */
    Calendar getTimestamp();

    void play();

    @Override
    int compareTo(ListableMessage other);

    boolean isSent();

    boolean isRead();


}
