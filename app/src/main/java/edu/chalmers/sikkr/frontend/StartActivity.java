package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;
import edu.chalmers.sikkr.backend.sms.TheInbox;


public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        ContactBook.setupSingleton(this);
        TextToSpeechUtility.setupTextToSpeech(this);
        TheInbox.setupInbox(this);

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
    }


    }



}

