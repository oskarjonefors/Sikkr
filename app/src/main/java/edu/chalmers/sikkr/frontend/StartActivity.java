package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.mms.MMSInbox;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.ProgressListener;
import edu.chalmers.sikkr.backend.util.SpeechRecognitionHelper;
import edu.chalmers.sikkr.backend.util.SystemData;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;
import edu.chalmers.sikkr.backend.util.VoiceMessagePlayer;
import edu.chalmers.sikkr.backend.util.VoiceMessageRecorder;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;





public class StartActivity extends Activity {
    private ArrayList<String> matches;
    private final static int MY_TTS_CHECK_CODE = 1337;

    private String text;
    private Intent intent;
    private String[] words;
    private Contact contact;
    private static final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LogUtility.writeLogFile(TAG, true, "Works before the initializer.");
        new Initializer().execute(this);
    }


    /**
     * Actionhandler for this activity
     *
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
        }
    }

    /**
     * A method to retrieve results from finished speech recognition.
     * Will trigger methods to match certain keywords
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                text = matches.get(0);
                callContactByName();
                selectFunctionality();
            }
        } else if (requestCode == MY_TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                TextToSpeechUtility.setupTextToSpeech(this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }

        }
    }

    /**
     * Method to check if voice recognition was used to select functionality
     * Will redirect user to the selected activity
     */
    private void selectFunctionality() {
        if (text.equals("1") || text.contains("senast")) {
            intent = new Intent(this, LatestCallsActivity.class);
            startActivity(intent);
        } else if (text.equals("2") || text.contains("favor")) {
            intent = new Intent(this, ContactGridActivity.class);
            startActivity(intent);
        } else if (text.equals("3") || text.contains("med") || text.contains("inkorg") || text.contains("sm")) {
            intent = new Intent(this, SMS_Activity.class);
            startActivity(intent);
        } else if (text.equals("4") || text.contains("bok") || text.contains("kontakt")) {
            words = text.split(" ");
            if (words.length > 1) {
                intent = new Intent(this, ContactGridActivity.class);
                intent.putExtra("initial_letter", words[1].charAt(0));
                startActivity(intent);
            } else {
                intent  = new Intent(this, ContactBookActivity.class);
                startActivity(intent);
            }

        }

    }

    /**
     * Method to check if voice recognition was used to make a call
     * Will try to call contact that best matches the input.
     */
    private void callContactByName() {
        final ContactBook cb = ContactBook.getSharedInstance();
        words = text.split(" ");
        try {
            if (words[0].contains("ing")) {
                intent = new Intent(Intent.ACTION_CALL);
                String searchString = "";
                for (int i = 1; i < words.length; i++) {
                    searchString += words[i] + " ";
                }
                contact = cb.getClosestMatch(searchString);
            }
            if (contact != null) {
                if (contact.getDefaultNumber() != null && contact.getName() != null) {
                    intent.setData(Uri.parse("tel:" + contact.getDefaultNumber()));
                    TextToSpeechUtility.readAloud("ringer " + contact.getName());
                    while (TextToSpeechUtility.isSpeaking()) {
                        Thread.sleep(100);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        } catch (Throwable t) {
        }

    }

    void updateProgress(double progress, String taskMsg) {

        ProgressBar initBar = (ProgressBar) findViewById(R.id.initProgressBar);
        TextView initText = (TextView) findViewById(R.id.initTextView);

        if (initBar != null && initText != null) {
            initBar.setProgress((int) (progress * initBar.getMax()));
            initText.setText(taskMsg + "...");
        }
    }

    private class Initializer extends AsyncTask<StartActivity, String, Boolean> implements ProgressListener {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.init_screen);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setContentView(R.layout.activity_start);
            } else {
                throw new RuntimeException("Initialization failed!");
            }
        }

        @Override
        protected Boolean doInBackground(StartActivity... params) {
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_TTS_CHECK_CODE);
            ContactBook.setupSingleton(params[0], this);
            TextToSpeechUtility.setupTextToSpeech(params[0]);
            TheInbox.setupInbox(params[0]);
            CallLog.setUpCallLog(params[0]);
            VoiceMessagePlayer.setupSingleton(params[0]);
            VoiceMessageRecorder.setupSingleton(params[0]);
            VoiceMessageSender.setupSingleton(params[0]);
            try {
                MMSInbox.setContext(params[0]);
                MMSInbox.getSharedInstance().loadInbox();
            } catch (Throwable t) {
                final List<String> trace = new ArrayList<String>();
                for (StackTraceElement el : t.getStackTrace()) {
                    trace.add("" + el);
                }
                LogUtility.writeLogFile(TAG, trace.toArray(new String[trace.size()]));
            }
            return true;
        }

        @Override
        public void onProgressUpdate(String... values) {
            updateProgress(Double.parseDouble(values[0]), values[2]);
        }

        @Override
        public void notifyProgress(double progress, String senderTag, String taskMsg) {
            publishProgress(progress + "", senderTag, taskMsg);
        }
    }
}

