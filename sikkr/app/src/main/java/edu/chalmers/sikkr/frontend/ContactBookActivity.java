package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;

import java.util.ArrayList;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.SpeechRecognitionHelper;
import edu.chalmers.sikkr.backend.util.SystemData;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;


public class ContactBookActivity extends Activity {

    private ArrayList<String> matches;
    private BroadcastReceiver reciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_contact_book);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ButtonAdapter(this));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                final char firstChar = matches.get(0).charAt(0);

                if(ContactBook.getSharedInstance().getContacts(firstChar).size() > 0) {
                    Intent intent = new Intent(this, ContactGridActivity.class);
                    intent.putExtra("initial_letter", firstChar);
                    startActivity(intent);
                } else {
                    TextToSpeechUtility.readAloud(getString(
                            R.string.found_no_contacts_initial_letter) + " " + firstChar);
                }
            }
        }
    }

    public void voiceSearch(View view) {
        SpeechRecognitionHelper.run(this);
    }


}
