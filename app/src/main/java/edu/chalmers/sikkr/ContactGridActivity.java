package edu.chalmers.sikkr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.chalmers.sikkr.backend.Contact;
import edu.chalmers.sikkr.backend.ContactBook;


public class ContactGridActivity extends Activity {

    private GridView gridView;
    private ContactViewAdapter adapter;
    private Set<Contact> contacts;

    public ContactGridActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContactBook book = new ContactBook(this);
        contacts = book.getContacts();

        setContentView(R.layout.activity_contact_grid);

        gridView = (GridView) findViewById(R.id.contact_grid);

        final List<Contact> contactList = new ArrayList<Contact>();

        for(Contact c : contacts) {
            contactList.add(c);
        }

        adapter = new ContactViewAdapter(this, R.layout.contact_thumb, contactList);
        gridView.setAdapter(adapter);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
