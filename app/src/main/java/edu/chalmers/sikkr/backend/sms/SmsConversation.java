package edu.chalmers.sikkr.backend.sms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jingis on 2014-10-03.
 */
public class SmsConversation {
    final private String phoneNbr;
    final private String contactName;
    final private List<OneSms> smsConversation = new ArrayList<OneSms>();

    public SmsConversation(String phoneNbr, String name) {
        this.phoneNbr = phoneNbr;
        contactName = name;
    }

    public void addSms(OneSms sms) {
        smsConversation.add(sms);
    }
    public String getAddress() {
        return phoneNbr;
    }

    public String getContactName() {
        return contactName;
    }
    public List<OneSms> getSmsList() {
        return smsConversation;
    }
}
