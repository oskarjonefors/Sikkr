package edu.chalmers.sikkr.backend.messages;

import java.util.HashSet;
import java.util.Set;

import edu.chalmers.sikkr.backend.ListableMessage;

/**
 * Created by Jingis on 2014-10-03.
 */
public class Conversation {
    final private String phoneNbr;
    final private String latestDate;
    final private boolean isSent;
    final private Set<ListableMessage> smsConversation = new HashSet<ListableMessage>();

    public Conversation(String adress, String person, String latestDate, boolean isSent) {
        this.phoneNbr = adress;
        this.latestDate = latestDate;
        this.isSent = isSent;
    }

    public void addMessage(ListableMessage msg) {
        smsConversation.add(msg);
    }
    public String getAddress() {
        return phoneNbr;
    }
    public boolean isSent(){
        return isSent;
    }
    public Set<ListableMessage> getSmsList() {
        return smsConversation;
    }
    public String getLatestDate() {
        return latestDate;
    }
}
