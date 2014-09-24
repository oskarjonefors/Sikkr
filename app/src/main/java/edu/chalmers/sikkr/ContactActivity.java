package edu.chalmers.sikkr;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.net.Uri;
import android.util.Log;


public class ContactActivity extends Activity {

    private TextView contactName = (TextView)findViewById(R.id.contactName);


    public void buttonClick() {
        //Brings out the phone dialer
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        //sets the data for which number to call, in this case Joel
        phoneIntent.setData(Uri.parse("tel:0736958002"));
        try{
            startActivity(phoneIntent);
            finish();
            Log.i("Finished making a call","");
        }catch(ActivityNotFoundException e){
            Log.v("Exception ocurred, could not make a call","");


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contactName.setText(getIntent().getDataString());
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
