package edu.chalmers.sikkr.backend.messages;

/**
 * Created by Jesper on 2014-10-15.
 */
public abstract class AbstractMessage implements ListableMessage {

    @Override
    public int compareTo(ListableMessage otherMessage){
        return getTimestamp().compareTo(otherMessage.getTimestamp());
    }
}
