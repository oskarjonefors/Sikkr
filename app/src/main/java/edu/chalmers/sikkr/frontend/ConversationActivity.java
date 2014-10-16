package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ListableMessage;
import edu.chalmers.sikkr.backend.MessageNotSentException;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;

public class ConversationActivity extends Activity {
    private SmsConversation thisConversation;
    private Set<ListableMessage> messageSet;
    private VoiceMessageRecorder recorder;
    private ImageButton sendButton;
    private ImageButton cancelButton;
    private ImageButton recordButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        createConversationLayout();
        recorder = VoiceMessageRecorder.getSharedInstance();
        sendButton = (ImageButton)findViewById(R.id.conversation_send);
        cancelButton = (ImageButton)findViewById(R.id.conversation_cancel);
        recordButton = (ImageButton)findViewById(R.id.conversation_record);
        recordButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.GONE);
        sendButton.setEnabled(false);
        cancelButton.setEnabled(false);
        cancelButton.setVisibility(View.GONE);

    }
    public void createConversationLayout(){
        final Bundle bundle = getIntent().getExtras();
        if(bundle!=null && bundle.containsKey("position") && bundle.containsKey("name")){
            thisConversation = SMS_Activity.getConversations().get(bundle.getInt("position"));
            TextView tv = (TextView)findViewById(R.id.conversation_name);
            tv.setText(bundle.getString("name"));
            messageSet = new HashSet<ListableMessage>();
            messageSet = thisConversation.getSmsList();
            List<ListableMessage> messages = new ArrayList<ListableMessage>();
            messages.addAll(messageSet);
            Collections.sort(messages);
            ArrayAdapter adapter = new ConversationAdapter(this, R.layout.conversationitem_left,messages );
            ListView listV = (ListView)findViewById(R.id.conversation_list);
            listV.setAdapter(adapter);
        }
    }

    public void cancelMessage(){
        recorder.getVoiceMessage();
        cancelButton.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
        cancelButton.setEnabled(false);
        sendButton.setEnabled(false);
        recordButton.setEnabled(true);
    }

    public void recordMessage(){
        switch (recorder.getRecordingState()) {
            case RESET:
                recorder.startRecording();
                recordButton.setBackgroundResource(R.drawable.stop_record);
                break;
            case RECORDING:
                recorder.stopRecording();
                recordButton.setBackgroundResource(R.drawable.start_record);
                sendButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                sendButton.setEnabled(true);
                cancelButton.setEnabled(true);
                recordButton.setEnabled(false);
                break;
        }
    }

    public void sendMessage(){
        VoiceMessageSender sender = VoiceMessageSender.getSharedInstance();
        try {
            sender.sendMessage(recorder.getVoiceMessage(), thisConversation.getAddress());
        } catch (MessageNotSentException e) {
            Log.e("ContactActivity", "Message not sent");
        }
    }

    public void readMessage(View view){
        ((ListableMessage)view.getTag()).play();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
