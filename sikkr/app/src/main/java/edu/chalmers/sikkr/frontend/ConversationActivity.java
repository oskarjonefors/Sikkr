package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.messages.Conversation;
import edu.chalmers.sikkr.backend.messages.InboxDoneLoadingListener;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.messages.PlaybackListener;
import edu.chalmers.sikkr.backend.messages.TheInbox;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;
import edu.chalmers.sikkr.backend.util.ServerInterface;
import edu.chalmers.sikkr.backend.util.SoundClipPlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;

/**
 * A class to represent Message Conversation with specific contact
 * @author Jesper Olsson
 */
public class ConversationActivity extends Activity implements InboxDoneLoadingListener {
    private Conversation thisConversation;
    private final List<ListableMessage> messages = new ArrayList<>();
    private VoiceMessageRecorder recorder;
    private ImageButton sendButton;
    private ImageButton cancelButton;
    private Button recordButton;
    private BroadcastReceiver broadcastReceiver;
    ArrayAdapter<ListableMessage> adapter;

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

    }
    @Override
    protected void onResume(){
        super.onResume();
        /**
         * Receiver to handle incoming text messages dynamically
         */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TheInbox.getInstance().loadInbox(ConversationActivity.this);
                adapter.notifyDataSetChanged();
                adapter.setNotifyOnChange(true);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }
    @Override
    protected void onPause(){
        super.onPause();
        try{
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){

        }
        try {
            if (recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.RECORDING) {
                recorder.stopRecording();
                recorder.discardRecording();
            } else if (recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.STOPPED) {
                recorder.discardRecording();
            }
        } catch (IOException e) {

        }
    }
    protected void onDestroy(){
        super.onDestroy();
        try{
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){

        }
    }

    /**
     * Method to make the recording button appear and the other buttons disappear
     */
    private void setButtonVisability() {
        sendButton = (ImageButton) findViewById(R.id.conversation_send);
        cancelButton = (ImageButton) findViewById(R.id.conversation_cancel);
        recordButton = (Button) findViewById(R.id.conversation_record);
        recordButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.GONE);
        sendButton.setEnabled(false);
        cancelButton.setEnabled(false);
        cancelButton.setVisibility(View.GONE);


    }

    /**
     * Method to be run in onCreate. Setup all thing needed for this activity.
     */
    public void createConversationLayout(){
        final Bundle bundle = getIntent().getExtras();
        if(bundle!=null && bundle.containsKey("number") && bundle.containsKey("name")){
            thisConversation = TheInbox.getInstance().getConversation(bundle.getString("number"));
            TextView tv = (TextView)findViewById(R.id.conversation_name);
            tv.setText(bundle.getString("name"));
            messages.addAll(thisConversation.getSmsList());
            Collections.sort(messages);
            adapter = new ConversationAdapter(this, messages );
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
                SoundClipPlayer.playSound(this, R.raw.rec_beepbeep, new RecordingWaiter());

                recordButton.setBackgroundResource(R.drawable.rec_button_active);
                Animatable anim = (Animatable) recordButton.getBackground();
                anim.start();
                break;
            case RECORDING:
                recordButton.setVisibility(View.INVISIBLE);
                recorder.stopRecording();
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
        final VoiceMessage vmsg = recorder.getVoiceMessage();
        VoiceMessageSender.getSharedInstance().sendMessage(vmsg, thisConversation.getAddress());
        vmsg.markAsRead();
        thisConversation.addMessage(vmsg);
        onDone();
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
        recordButton.setBackgroundResource(R.drawable.rec_button);
        recordButton.setVisibility(View.VISIBLE);
    }

    /**
     * OnClick for the play button.
     * Will read the given text message out aloud
     * @param view the view that called this method
     */
    public void readMessage(View view){

        Button trybutton =  (Button)view.findViewById(R.id.conversation_icon);

        final ListableMessage msg = (ListableMessage)view.getTag();

        msg.play(new PlayButtonHandler(trybutton, msg, this));
        msg.markAsRead();
    }

    @Override
    public void onDone() {
        adapter.clear();
        adapter.addAll(thisConversation.getSmsList());
    }

    class RecordingWaiter implements PlaybackListener {

        @Override
        public void playbackStarted() {
            /* Do a little dance */
        }

        @Override
        public void playbackDone() {
            recorder.startRecording();
        }

        @Override
        public void playbackError() {
            playbackDone();
        }
    }
}
