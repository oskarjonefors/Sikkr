package edu.chalmers.sikkr.backend.messages;

import java.util.Set;
import java.util.TreeSet;

import edu.chalmers.sikkr.backend.util.MessageUtils;

/**
 * Created by Jingis on 2014-10-03.
 */
public class Conversation {
    private String phoneNbr;
    private final String fixedNumber;
    final private Set<ListableMessage> conversation;

    public Conversation(String address, boolean fixed) {
        conversation = new TreeSet<>();
        if (fixed) {
            fixedNumber = address;
        } else {
            phoneNbr = address;
            fixedNumber = MessageUtils.fixNumber(address);
        }
    }

    public void setLocalNumber(String number) {
        phoneNbr = number;
    }
    public boolean hasLocalNumber() {
        return phoneNbr != null && !phoneNbr.isEmpty();
    }
    public void addMessage(ListableMessage msg) {
        conversation.add(msg);
    }
    public String getAddress() {
        if (!hasLocalNumber()) {
            return getFixedNumber();
        } else {
            return phoneNbr;
        }
    }
    public String getFixedNumber() {
        return fixedNumber;
    }
    public ListableMessage getLatestMessage() {
        return (ListableMessage) conversation.toArray()[conversation.size() - 1];
    }
    public Set<ListableMessage> getSmsList() {
        return conversation;
    }
}
