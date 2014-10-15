package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;


public class ContactGridActivity extends Activity {

    public ContactGridActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContactBook book = ContactBook.getSharedInstance();
        final Bundle bundle = getIntent().getExtras();
        final Set<Contact> contacts;

        if(bundle != null && bundle.containsKey("initial_letter")) {
            contacts = book.getContacts(bundle.getChar("initial_letter"));
        } else {
            contacts = book.getFavoriteContacts();
        }

        final List<Contact> contactList = new ArrayList<Contact>();
        for(final Contact c : contacts) {
            contactList.add(c);
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_grid);

        ContactViewAdapter adapter = new ContactViewAdapter(this, R.layout.contact_thumb, contactList);
        ((GridView) findViewById(R.id.contact_grid)).setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
