package edu.chalmers.sikkr;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import java.util.List;

        import edu.chalmers.sikkr.backend.util.OneSms;
        import edu.chalmers.sikkr.backend.util.TheInbox;


public class sms_activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_layout);
        createSmsLayout();
    }

    private void createSmsLayout() {
        List<OneSms> smsList = TheInbox.getInstance().getSmsInbox();
        String[] msg = new String[smsList.size()];
        int index = 0;
        for(OneSms sms: smsList ) {
            msg[index] = sms.getMessage();
            index += 1;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sms_element, msg );
        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(adapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sms_activity, menu);
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
