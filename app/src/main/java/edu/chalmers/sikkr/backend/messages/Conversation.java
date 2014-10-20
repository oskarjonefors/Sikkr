package edu.chalmers.sikkr.backend.messages;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Jingis on 2014-10-03.
 */
public class Conversation {
    final private String phoneNbr;
    final private Set<ListableMessage> conversation;

    public Conversation(String address) {
        this.phoneNbr = address;
        conversation = new TreeSet<>();
    }

    public void addMessage(ListableMessage msg) {
        conversation.add(msg);
    }
    public String getAddress() {
        return phoneNbr;
    }
    public Set<ListableMessage> getSmsList() {
        return conversation;
    }
}
