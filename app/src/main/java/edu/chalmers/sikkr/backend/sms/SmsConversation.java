package edu.chalmers.sikkr.backend.sms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.backend.ListableMessage;

/**
 * Created by Jingis on 2014-10-03.
 */
public class SmsConversation{
    private int  count;
    final private String phoneNbr;
    final private String latestDate;
    final private boolean isSent;
    final private Set<ListableMessage> smsConversation = new HashSet<ListableMessage>();

    public SmsConversation(String adress, String latestDate, boolean isSent) {
        this.phoneNbr = adress;
        this.latestDate = latestDate;
        this.isSent = isSent;
    }

    public void addSms(OneSms sms) {
        smsConversation.add(sms);
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
    public void setCount(int x) {
        count = x;
    }
}
