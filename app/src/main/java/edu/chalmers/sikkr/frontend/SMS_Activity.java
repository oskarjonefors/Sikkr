package edu.chalmers.sikkr.frontend;

        import android.app.Activity;
        import android.content.ContentResolver;
        import android.content.Context;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Bundle;

        import android.provider.BaseColumns;
        import android.provider.ContactsContract;
        import android.provider.Telephony;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;
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
        //Toast.makeText(this, ((OneSms) view.getTag()).getMessage(), Toast.LENGTH_LONG ).show();
        ((OneSms) view.getTag()).play();

    }

    public String getContactByNbr(String number) {
        String name = "?";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{ BaseColumns._ID,
                    ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                cursor.moveToNext();
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

            } catch(Exception e) {

            }
            finally {
                cursor.close();
            }
        }
        return name;
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
        public View getView(int i, View v, ViewGroup viewGroup) {
            View view = v;
            final ViewHolder holder;

            if (view == null) {
                Toast.makeText(context, "first run", Toast.LENGTH_SHORT).show();
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);
                holder = new ViewHolder();
                holder.contactName = (TextView)view.findViewById(R.id.sender);
                view.setTag(holder);
            } else {
                Toast.makeText(context, "second run", Toast.LENGTH_SHORT).show();
                holder = (ViewHolder)view.getTag();
            }

            //get the current sms conversation
            SmsConversation currentConv = list.get(i);


            //Link an sms to the playbutton
            view.findViewById(R.id.imageButton).setTag(currentConv.getSmsList().get(0));

            //set the info of the element
            holder.contactName.setText((currentConv.getAddress()));
/*
                //view for date
                TextView dateView = (TextView)view.findViewById(R.id.date);
                dateView.setText(currentConv.getSmsList().get(0).getDate());
*/
            return view;
        }
    }

    static class ViewHolder {
        TextView contactName;
    }
}

