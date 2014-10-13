package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.util.Log;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.MessageNotSentException;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;


public class ContactActivity extends Activity {

    private Contact contact;
    private VoiceMessageRecorder recorder;

    public void buttonClick(View view) {
        //Brings out the phone dialer
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);

        //contact.getMobilePhoneNumbers().get(0);
        //contact.getPhoneNumbers().get(0)))
        //Sets the data for which number to call
        phoneIntent.setData(Uri.parse("tel:" + contact.getPhoneNumbers().get(0)));
        try{
            startActivity(phoneIntent);
            finish();
            Log.i("Finished making a call","");
        }catch(ActivityNotFoundException e){
            Log.v("Exception ocurred, could not make a call","");
        }
    }

    public void voiceInteraction(View view) {
        final Button btn = (Button)findViewById(R.id.recordButton);
        switch (recorder.getRecordingState()) {
            case RESET:
                recorder.startRecording();
                btn.setText("Stop recording...");
                break;
            case RECORDING:
                recorder.stopRecording();
                btn.setText("Send");
                break;
            case STOPPED:
                VoiceMessageSender sender = VoiceMessageSender.getSharedInstance();
                try {
                    sender.sendMessage(recorder.getVoiceMessage(), contact.getMobilePhoneNumbers().get(0));
                } catch (MessageNotSentException e) {
                    Log.e("ContactActivity", "Message not sent");
                }
                btn.setText("Record");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact);

        //This makes us able to use the TextView defined in the .xml and change it from here
        final TextView contactName = (TextView)findViewById(R.id.contactName);
        final TextView contactNumber = (TextView)findViewById(R.id.contactNumber);
        final ImageView contactPicture = (ImageView)findViewById(R.id.contactPicture);


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact, menu);
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
