package edu.chalmers.sikkr.backend.sms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jingis on 2014-10-03.
 */
public class SmsConversation{
    final private String phoneNbr;
    final private String contactName;
    final private String latestDate;
    final private boolean isSent;
    final private List<OneSms> smsConversation = new ArrayList<OneSms>();

    public SmsConversation(String adress, String person, String latestDate, boolean isSent) {
        this.phoneNbr = adress;
        contactName = person;
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
    public String getContactName() {
        return contactName;
    }
    public List<OneSms> getSmsList() {
        return smsConversation;
    }
    public String getLatestDate() {
        return latestDate;
    }
}
