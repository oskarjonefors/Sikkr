package edu.chalmers.sikkr.frontend;

        import android.app.Activity;
        import android.content.Context;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.TextView;
        import java.util.ArrayList;
        import java.util.List;

        import edu.chalmers.sikkr.R;
        import edu.chalmers.sikkr.backend.sms.OneSms;
        import edu.chalmers.sikkr.backend.sms.SmsConversation;
        import edu.chalmers.sikkr.backend.sms.TheInbox;
        import edu.chalmers.sikkr.backend.util.TextToSpeechUtility;


public class SMS_Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_layout);
        createSmsLayout();
    }

    private void createSmsLayout() {
        /*
        List<OneSms> smsList = TheInbox.getInstance().getSmsInbox();
        String[] msg = new String[smsList.size()];

        int index = 0;
        for(OneSms sms: smsList ) {
            msg[index] = sms.getMessage();
            index += 1;
        }
        */

        ArrayList<SmsConversation> smsList = TheInbox.getInstance().getSmsInbox();

        ArrayAdapter adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
        ListView listV = (ListView)findViewById(R.id.listView);
        listV.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items t
        // o the action bar if it is present.
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

    
    public void readMsg(View view) {
        TextToSpeechUtility.readAloud((String) (view.getTag()));
    }

    //Inner adapterclass
    public class SmsViewAdapter extends ArrayAdapter {

        private final Context context;
        private final List<SmsConversation> list;
        private final int layoutId;

        private SmsViewAdapter(Context context, int layoutId, List list) {
            super(SMS_Activity.this, layoutId, list);
            this.context = context;
            this.list = list;
            this.layoutId = layoutId;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);

                //get the current sms conversation
                SmsConversation currentConv = list.get(i);
                view.findViewById(R.id.imageButton).setTag(currentConv.getSmsList().get(0));

                //view for the contact
                TextView sender = (TextView)view.findViewById(R.id.sender);
                sender.setText(currentConv.getAddress());
/*
                //view for date
                TextView dateView = (TextView)view.findViewById(R.id.date);
                dateView.setText(currentConv.getSmsList().get(0).getDate());
*/

            }
            return view;
        }
    }

}

