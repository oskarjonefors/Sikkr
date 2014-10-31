package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.messages.PlaybackListener;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.SoundClipPlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;


public class ContactActivity extends Activity {

    private Contact contact;
    private VoiceMessageRecorder recorder;

    private Button sendButton;
    private Button cancelButton;
    private Button recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_contact);
        setButtonVisibility();

        //This makes us able to use the TextView defined in the .xml and change it from here
        final TextView contactName = (TextView) findViewById(R.id.contactName);
        final TextView contactNumber = (TextView) findViewById(R.id.contactNumber);
        final ImageView contactPicture = (ImageView) findViewById(R.id.contactPicture);


        //Gets the shared instance ContactBook
        final ContactBook book = ContactBook.getSharedInstance();
        //Saves the content of the intent in a bundle
        final Bundle bundle = getIntent().getExtras();

        //From the bundle we get the contact ID,
        //and with it we get the right contact from the Contact Book
        contact = book.getContact(bundle.getString("contact_id"));

        //Set the name of the contact as the text in the TextView
        contactName.setText(contact.getName());
        //Set the picture of the contact in the ImageView
        contactPicture.setImageBitmap(contact.getPhoto());

        //Set the first phonenumber of the contact in the ImageView
        contactNumber.setText(contact.getMobilePhoneNumbers().get(0));

        recorder = VoiceMessageRecorder.getSharedInstance();

    }
    @Override
    public void onPause(){
        super.onPause();
        if(recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.RECORDING) {
            recorder.stopRecording();
            try {
                recorder.discardRecording();
            } catch (IOException e) {

            }
        }else if(recorder.getRecordingState() == VoiceMessageRecorder.RecordingState.STOPPED){
            try {
                recorder.discardRecording();
            } catch (IOException e) {

            }
        }
    }

    public void buttonClick(View view) {
        //Brings out the phone dialer
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);

        //Sets the data for which number to call
        phoneIntent.setData(Uri.parse("tel:" + contact.getPhoneNumbers().get(0)));
        try {
            startActivity(phoneIntent);
            finish();
            Log.i("Finished making a call", "");
        } catch (ActivityNotFoundException e) {
            Log.v("Exception ocurred, could not make a call", "");
        }
    }

    public void voiceInteraction(View view) {
        final Button btn = (Button) findViewById(R.id.recordButton);
        switch (recorder.getRecordingState()) {
            case RESET:
                recorder.startRecording();
                btn.setText(R.string.stop_recording + "...");
                break;
            case RECORDING:
                recorder.stopRecording();
                btn.setText(R.string.send);
                break;

        }
    }

    public void recordTheMessage(View v){
        switch (recorder.getRecordingState()) {
            case RESET:
                SoundClipPlayer.playSound(this, R.raw.rec_beepbeep, new RecordingWaiter());
                recorder.startRecording();
                recordButton.setBackgroundResource(R.drawable.rec_button_active);
                AnimationDrawable anim = (AnimationDrawable) recordButton.getBackground();
                anim.start();
                break;
            case RECORDING:
                recorder.stopRecording();
                recordButton.setVisibility(View.INVISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                sendButton.setEnabled(true);
                cancelButton.setEnabled(true);
                recordButton.setEnabled(false);
                break;
        }
    }

    private void setButtonVisibility() {
         sendButton = (Button) findViewById(R.id.conversation_send);
         cancelButton = (Button) findViewById(R.id.conversation_cancel);
         recordButton = (Button) findViewById(R.id.recordButton);
         recordButton.setVisibility(View.VISIBLE);
         sendButton.setVisibility(View.GONE);
         sendButton.setEnabled(false);
         cancelButton.setEnabled(false);
         cancelButton.setVisibility(View.GONE);
    }
    private void hideButtons() {
        cancelButton.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
        cancelButton.setEnabled(false);
        sendButton.setEnabled(false);
        recordButton.setEnabled(true);
        recordButton.setBackgroundResource(R.drawable.rec_button);
        recordButton.setVisibility(View.VISIBLE);
    }
    public void sendTheMessage(View v) {
        try {
            VoiceMessageSender sender = VoiceMessageSender.getSharedInstance();
            sender.sendMessage(recorder.getVoiceMessage(), contact.getMobilePhoneNumbers().get(0));
        } catch (Exception e) {
            Log.e("ContactActivity", "Message not sent");
        }
        hideButtons();
    }

    public void cancelTheMessage(View v) {
        try {
            recorder.discardRecording();
        } catch (IOException e) {

        }
        hideButtons();
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
