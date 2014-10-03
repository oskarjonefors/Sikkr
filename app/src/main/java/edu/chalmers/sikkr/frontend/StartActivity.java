package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import java.util.ArrayList;
import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.SystemData;
import edu.chalmers.sikkr.backend.mms.MMSInbox;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;
import edu.chalmers.sikkr.backend.sms.TheInbox;

import edu.chalmers.sikkr.backend.util.SpeechRecognitionHelper;

import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;



public class StartActivity extends Activity {
    private ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        ContactBook.setupSingleton(this);
        TextToSpeechUtility.setupTextToSpeech(this);
        TheInbox.setupInbox(this);
        CallLog.setUpCallLog(this);

        MMSInbox.setContext(this);
        MMSInbox.getSharedInstance().loadInbox();


        /*MMSInbox.setContext(this);
        MMSInbox.getSharedInstance().loadInbox();*/
        VoiceMessagePlayer.setupSingleton(this);
        VoiceMessageRecorder.setupSingleton(this);
        VoiceMessageSender.setupSingleton(this);

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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.size() >0){
                String text = matches.get(0);
                //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                Intent intent;
                if (text.equals("1")) {
                    intent = new Intent(this, LatestCallsActivity.class);
                    startActivity(intent);
                } else  if (text.equals("2")) {
                    intent = new Intent(this, ContactGridActivity.class);
                    startActivity(intent);
                } else if (text.equals("3")) {
                    intent = new Intent(this, SMS_Activity.class);
                    startActivity(intent);
                } else if (text.equals("4")) {
                    intent = new Intent(this, ContactBookActivity.class);
                    startActivity(intent);
                }
            }
        }
    }




}

