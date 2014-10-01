package edu.chalmers.sikkr;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import edu.chalmers.sikkr.backend.Contact;
import edu.chalmers.sikkr.backend.ContactBook;


public class ContactActivity extends Activity {

    private Contact contact;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact);

        //This makes us able to use the TextView defined in the .xml and change it from here
        final TextView contactName = (TextView)findViewById(R.id.contactName);
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
