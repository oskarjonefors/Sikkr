package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.messages.Conversation;
import edu.chalmers.sikkr.backend.messages.InboxDoneLoadingListener;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.messages.OneSms;
import edu.chalmers.sikkr.backend.messages.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.ServerInterface;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;

/**
 * A class to represent Message Conversation with specific contact
 * @author Jesper Olsson
 */
public class ConversationActivity extends Activity implements InboxDoneLoadingListener {
    private Conversation thisConversation;
    private Set<ListableMessage> messageSet;
    private final List<ListableMessage> messages = new ArrayList<>();
    private VoiceMessageRecorder recorder;
    private ImageButton sendButton;
    private ImageButton cancelButton;
    private ImageButton recordButton;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServerInterface.addSingletonInboxDoneLoadingListener(this);
        VoiceMessageSender.addInboxDoneLoadingListener(this);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        createConversationLayout();
        setButtonVisability();
        recorder = VoiceMessageRecorder.getSharedInstance();
        adapter.setNotifyOnChange(true);

        BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    String messageBody = smsMessage.getMessageBody();
                    String phoneNbr = smsMessage.getOriginatingAddress();
                    String date = String.valueOf(smsMessage.getTimestampMillis());
                    OneSms sms = new OneSms(messageBody, date, false);
                    List<Conversation> list = TheInbox.getInstance().getMessageInbox();
                    for(Conversation conv : list){
                        if(phoneNbr.equals(conv.getAddress())){
                            sms.markAsUnread();
                            conv.addMessage(sms);
                        }
                    }
                    if(phoneNbr.equals(thisConversation.getAddress())){
                        messages.add(sms);
                    }
                }
                adapter.notifyDataSetChanged();
                adapter.setNotifyOnChange(true);
                Collections.sort(messages);

            }
        };
        registerReceiver(broadcastReciever, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }
    @Override
    public void onPause(){
        super.onPause();
        try {
            if (recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.RECORDING) {
                recorder.stopRecording();
                recorder.discardRecording();
            } else if (recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.STOPPED) {
                recorder.discardRecording();
            }
        } catch (IOException e) {
            LogUtility.writeLogFile("ConversationActivityLogs", e);
        }
    }

    /**
     * Method to makee the recording button appear and the other buttons dissapear
     */
    private void setButtonVisability() {
        sendButton = (ImageButton) findViewById(R.id.conversation_send);
        cancelButton = (ImageButton) findViewById(R.id.conversation_cancel);
        recordButton = (ImageButton) findViewById(R.id.conversation_record);
        recordButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.GONE);
        sendButton.setEnabled(false);
        cancelButton.setEnabled(false);
        cancelButton.setVisibility(View.GONE);


    }

    /**
     * Mehtod to be run in onCreate. Setup all thing needed for this activity.
     */
    public void createConversationLayout(){
        final Bundle bundle = getIntent().getExtras();
        if(bundle!=null && bundle.containsKey("number") && bundle.containsKey("name")){
            thisConversation = TheInbox.getInstance().getConversation(bundle.getString("number"));
            TextView tv = (TextView)findViewById(R.id.conversation_name);
            tv.setText(bundle.getString("name"));
            messageSet = thisConversation.getSmsList();
            messages.addAll(messageSet);
            Collections.sort(messages);
            adapter = new ConversationAdapter(this, R.layout.conversationitem_left,messages );
            ListView listV = (ListView)findViewById(R.id.conversation_list);
            listV.setAdapter(adapter);
        }
    }

    /**
     * OnClick for the cancel button after voice message has been recorded.
     * Will delete the voice message that has been recorded
     * @param v the view that called this method
     */
    public void cancelMessage(View v){
        try {
            recorder.discardRecording();
            hideButtons();
        } catch (IOException e) {
            LogUtility.writeLogFile("ConversationActivity", e);
        }
    }

    /**
     * OnClick for the recording button.
     * Will start recording voice message
     * @param v the view that called this method
     */
    public void recordMessage(View v){
        switch (recorder.getRecordingState()) {
            case RESET:
                recorder.startRecording();
                recordButton.setBackgroundResource(R.drawable.stop_record);
                break;
            case RECORDING:
                recorder.stopRecording();
                recordButton.setBackgroundResource(R.drawable.redrec);
                sendButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                sendButton.setEnabled(true);
                cancelButton.setEnabled(true);
                recordButton.setEnabled(false);
                break;
        }
    }

    /**
     * OnClick for the send message button
     * Will send the voice message to the given contact in conversation
     * @param v the view that called this method
     */
    public void sendMessage(View v){
        VoiceMessageSender.getSharedInstance().sendMessage(recorder.getVoiceMessage(), thisConversation.getAddress());
        hideButtons();
    }

    /**
     * Method to show hide all buttons but the recording button
     */
    private void hideButtons(){
        cancelButton.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
        cancelButton.setEnabled(false);
        sendButton.setEnabled(false);
        recordButton.setEnabled(true);
    }

    /**
     * OnClick for the play button.
     * Will read the given text message out aloud
     * @param view the view that called this method
     */
    public void readMessage(View view){
        ((ListableMessage)view.getTag()).play();
        ((ListableMessage)view.getTag()).markAsRead();
        ImageButton trybutton =  (ImageButton)view.findViewById(R.id.conversation_icon);
        trybutton.setBackgroundResource(R.drawable.play);
    }

    @Override
    public void onDone() {
        LogUtility.writeLogFile("ConversationActivity", "Executing method onDone");
        adapter.clear();
        adapter.addAll(thisConversation.getSmsList());
    }
}
