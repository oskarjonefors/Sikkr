package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.calls.OneCall;
import edu.chalmers.sikkr.backend.contact.Contact;

public class LatestCallsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_calls);
        createCallLogLayout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.latest_calls, menu);
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

    private void createCallLogLayout() {
        if(!(CallLog.getInstance().getCallList() == null)) {
            List<OneCall> callList = CallLog.getInstance().getCallList();
            String[] nbr = new String[callList.size()];
            Contact contact;

            int index = 0;
            for (OneCall call : callList) {
                nbr[index] = call.getCallNumber();
                index++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sms_element, nbr);
            ListView list = (ListView) findViewById(R.id.listView);
            list.setAdapter(adapter);
        }
    }
}
