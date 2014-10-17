package edu.chalmers.sikkr.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.ArrayList;

import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.frontend.SMS_Activity;

/**
 * Created by Jesper on 2014-10-17.
 */

public class SmsListener extends BroadcastReceiver {
    private Context context;
    public SmsListener(Context context){
        this.context = context;
        IntentFilter filter1 = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        context.registerReceiver(SmsListener.this, filter1);
    }
    public void onReceive(Context context, Intent intent){
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();
                String phoneNbr = smsMessage.getOriginatingAddress();
                String date = String.valueOf(smsMessage.getTimestampMillis());
                OneSms sms = new OneSms(messageBody, phoneNbr, date, false);
                ArrayList<SmsConversation> list = SMS_Activity.getConversations();
                for(int i = 0;i<list.size();i++){
                    if(phoneNbr.equals(list.get(i).getAddress())){
                        sms.markAsUnread();
                        list.get(i).addSms(sms);
                    }
                }
            }
        }
    }
}
