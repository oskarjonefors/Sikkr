package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import android.widget.EditText;

import org.apache.commons.lang3.ObjectUtils;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.SystemData;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.SpeechRecognitionHelper;
import edu.chalmers.sikkr.backend.mms.MMSInbox;
import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;


public class StartActivity extends Activity {
    private ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SpeechRecognitionHelper.run(this);
        try {
            super.onCreate(savedInstanceState);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_start);
            ContactBook.setupSingleton(this);
            TextToSpeechUtility.setupTextToSpeech(this);
            TheInbox.setupInbox(this);
            CallLog.setUpCallLog(this);
            VoiceMessagePlayer.setupSingleton(this);
            VoiceMessageRecorder.setupSingleton(this);
            VoiceMessageSender.setupSingleton(this);

            MMSInbox.setContext(this);
            MMSInbox.getSharedInstance().loadInbox();
        } catch (Throwable t) {
            EditText text = (EditText) findViewById(R.id.editText);
            String s = "";
            for (StackTraceElement e : t.getStackTrace()) {
                s += e + "\n";
            }
            text.setText(s);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * Actionhandler for this activity
     * @param view
     */
    public void clickedButton(View view) {
        Intent intent;
        switch (view.getId()) {
        case R.id.contactBook:
            intent = new Intent(this, ContactBookActivity.class);
            startActivity(intent);
            break;
        case R.id.message:
            intent = new Intent(this, SMS_Activity.class);
            startActivity(intent);
            break;
        case R.id.fav_contacts:
            intent = new Intent(this, ContactGridActivity.class);
            startActivity(intent);
            break;
        case R.id.lastCall:
            intent = new Intent(this, LatestCallsActivity.class);
            startActivity(intent);
            break;
        case R.id.microphone:
            SpeechRecognitionHelper.run(this);
            break;

        }
    }

    /**
     * A method to retrieve results from finished speech recognition.
     * Will also match the results against certain keywords.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.size() >0){
            selectFunctionality();
            }
        }
    }

    private void selectFunctionality(){
        final ContactBook cb = ContactBook.getSharedInstance();
        String text = matches.get(0);
        String words[] = text.split(" ");
        Intent intent;
        Contact contact;
        try {
            if (words[0].contains("ing")) {
                intent = new Intent(Intent.ACTION_CALL);
                if (words.length > 2) {
                    contact = cb.getClosestMatch(words[1] + " " + words[2]);
                } else {
                    contact = cb.getClosestMatch(words[1]);
                }
                intent.setData(Uri.parse("tel:" + contact.getPhoneNumbers().get(0)));
            }
        }catch(ActivityNotFoundException ex){
            Log.d("FEL", "FEL");
        }catch(NullPointerException exe){
            Log.d("FEL", "INGEN KONTAKT FUNNEN");
        }
        if (text.equals("1") || text.contains("senaste")) {
            intent = new Intent(this, LatestCallsActivity.class);
            startActivity(intent);
        } else  if (text.equals("2") || text.contains("favorit")) {
            intent = new Intent(this, ContactGridActivity.class);
            startActivity(intent);
        } else if (text.equals("3") || text.contains("med") || text.contains("sms")) {
            intent = new Intent(this, SMS_Activity.class);
            startActivity(intent);
        } else if (text.equals("4") || text.contains("bok") || text.contains("kontakt")) {
            intent = new Intent(this, ContactBookActivity.class);
            startActivity(intent);
        }
    }




}

