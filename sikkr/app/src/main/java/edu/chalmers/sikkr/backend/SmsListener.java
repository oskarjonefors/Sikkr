package edu.chalmers.sikkr.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.List;

import edu.chalmers.sikkr.backend.messages.Conversation;
import edu.chalmers.sikkr.backend.messages.OneSms;
import edu.chalmers.sikkr.backend.messages.TheInbox;

/**
 * A class to handle incoming SMS
 * @author Jesper Olsson
 */

public class SmsListener extends BroadcastReceiver {
    public SmsListener(Context context){
        IntentFilter filter1 = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        context.registerReceiver(SmsListener.this, filter1);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();
                String phoneNbr = smsMessage.getOriginatingAddress();
                String date = String.valueOf(smsMessage.getTimestampMillis());
                OneSms sms = new OneSms(messageBody, date, false);
                List<Conversation> list = TheInbox.getInstance().getMessageInbox();
                for(Conversation conversation : list){
                    if(phoneNbr.equals(conversation.getAddress())){
                        sms.markAsUnread();
                        conversation.addMessage(sms);
                    }
                }
            }
        }
    }
}
