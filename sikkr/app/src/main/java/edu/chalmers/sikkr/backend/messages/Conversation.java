package edu.chalmers.sikkr.backend.messages;

import java.util.Set;
import java.util.TreeSet;

import edu.chalmers.sikkr.backend.util.MessageUtils;

/**
 * Created by Jingis on 2014-10-03.
 */
public class Conversation {
    private String phoneNbr;
    private String fixedNumber;
    final private Set<ListableMessage> conversation;

    private Conversation(String address, String fixedNumber) {
        conversation = new TreeSet<>();
        this.phoneNbr = address;
        this.fixedNumber = fixedNumber;
    }

    public Conversation(String address, boolean fixed) {
        conversation = new TreeSet<>();
        if (fixed) {
            fixedNumber = address;
        } else {
            phoneNbr = address;
            fixedNumber = MessageUtils.fixNumber(address);
        }
    }

    public void setFixedNumber(String number) {
        fixedNumber = number;
    }
    public boolean hasLocalNumber() {
        return phoneNbr != null && !phoneNbr.isEmpty();
    }
    public void addMessage(ListableMessage msg) {
        conversation.add(msg);
    }
    public String getAddress() {
        return phoneNbr;
    }
    public String getFixedNumber() {
        return fixedNumber;
    }
    public Set<ListableMessage> getSmsList() {
        return conversation;
    }
}
