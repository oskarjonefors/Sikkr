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
import java.util.ArrayList;
import java.util.List;


import android.widget.EditText;
import android.widget.Toast;


import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.LogUtility;
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

    private String text;
    private Intent intent;
    private String[] words;
    private Contact contact;
    public static final String TAG = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

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

        try {
            MMSInbox.setContext(this);
            MMSInbox.getSharedInstance().loadInbox();
        } catch (Throwable t) {
            final List<String> trace = new ArrayList<String>();
            for (StackTraceElement el : t.getStackTrace()) {
                trace.add("" + el);
            }
            LogUtility.writeLogFile(TAG, trace.toArray(new String[trace.size()]));
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
<<<<<<< HEAD
=======

    }

>>>>>>> Voice recognition now works for contact book as well.

        }

    }


    /**
     * A method to retrieve results from finished speech recognition.
     * Will trigger methods to match certain keywords
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.size() >0){
<<<<<<< HEAD
                text = matches.get(0);
                callContactByName();
                selectFunctionality();

            }
        }
    }

    /**
     * Method to check if voice recognition was used to select functionality
     * Will redirect user to the selected activity
     */
    private void selectFunctionality(){
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
            words = text.split(" ");
            if(words.length >1){
                Toast.makeText(this, words[0] + " " + words[1], Toast.LENGTH_LONG).show();
                intent = new Intent(this, ContactGridActivity.class);
                intent.putExtra("initial_letter",words[1].charAt(0));
                startActivity(intent);
            }else {
                intent = new Intent(this, ContactBookActivity.class);
                startActivity(intent);
            }
        }
    }

    /**
     * Method to check if voice recognition was used to make a call
     * Will try to call contact that best matches the input.
     */
    private void callContactByName(){
        final ContactBook cb = ContactBook.getSharedInstance();
        words = text.split(" ");
        try {
            if (words[0].contains("ing")) {
                intent = new Intent(Intent.ACTION_CALL);
                String searchString ="";
                for(int i = 1; i<words.length;i++){
                    searchString += words[i] +" ";
                }
                contact = cb.getClosestMatch(searchString);
                if(contact.getDefaultNumber() != null ) {
                    intent.setData(Uri.parse("tel:" + contact.getDefaultNumber()));
                    TextToSpeechUtility.readAloud("Ringer " + contact.getName());
                    LogUtility.writeLogFile("tjenare", "Kontaktnamn: " +contact.getName());
                    LogUtility.writeLogFile("tjenare", "Default Number: " +contact.getDefaultNumber());
                    LogUtility.writeLogFile("tjenare", "Phone Number: " +contact.getPhoneNumbers().get(0));
=======
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
>>>>>>> Voice recognition now works for contact book as well.
                    startActivity(intent);
                    finish();

                }
            }
        }catch(Throwable t) {
            final List<String> trace = new ArrayList<String>();
            for (StackTraceElement el : t.getStackTrace()) {
                trace.add("" + el);
            }
            LogUtility.writeLogFile("tjenare", "Kontaktnamn " + contact.getName());
            LogUtility.writeLogFile("tjenare", trace.toArray(new String[trace.size()]));
        }
    }




}

