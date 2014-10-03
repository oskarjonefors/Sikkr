package edu.chalmers.sikkr.backend.sms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jingis on 2014-10-03.
 */
public class SmsConversation {
    private String phoneNbr;
    private String contactName;
    private List<OneSms> smsConversation = new ArrayList<OneSms>();

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
}
