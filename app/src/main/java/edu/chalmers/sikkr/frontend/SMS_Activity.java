package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
<<<<<<< HEAD
=======
import android.graphics.Paint;
>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD
import java.util.ArrayList;
=======
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;

public class SMS_Activity extends Activity {
    ArrayList<SmsConversation> smsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_layout);
        createSmsLayout();

    }

    private void createSmsLayout() {
        smsList = TheInbox.getInstance().getSmsInbox();
        ArrayAdapter adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
        ListView listV = (ListView) findViewById(R.id.listView);
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

    /*
    Open the clicked contacts sms history view
     */
    public void clickedText(View view) {
        int position = (Integer) view.getTag();
        Toast.makeText(view.getContext(), "trycker på text med address:" + smsList.get(position).getAddress(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), ConversationActivity.class);
        intent.putExtra("phoneNumber", smsList.get(position).getAddress());
        intent.putExtra("contactName", getContactByNbr(smsList.get(position).getAddress()));
        startActivity(intent);
    }


    public String getContactByNbr(String number) {
        String contact = "";

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{ BaseColumns._ID,
                    ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            try {
                cursor.moveToNext();
                contact = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            } catch(Exception e) {
<<<<<<< HEAD
                Toast.makeText(this, "EXCEPTION. tryed to move cursor to first", Toast.LENGTH_SHORT).show();
=======
                Toast.makeText(this, "EXCEPTION. tried to move cursor to first", Toast.LENGTH_SHORT).show();
>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
            }
            finally {
                cursor.close();
            }
        }
        if(contact.length() == 0)
            return number;
        return contact;
    }

<<<<<<< HEAD
=======
    public String getPropperDate(String s) {
        Long dateNbr = Long.parseLong(s);
        Date date = new Date(dateNbr);
        String formattedDate = new SimpleDateFormat("EEE, MMM d, ''yy").format(date);
        return formattedDate;

    }

>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648

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
        public View getView(int i, final View v, ViewGroup viewGroup) {
            View view = v;
            final ViewHolder holder;

            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);
                holder = new ViewHolder();
                holder.contactName = (TextView) view.findViewById(R.id.sender);
<<<<<<< HEAD
=======
                holder.date = (TextView) view.findViewById(R.id.date);
>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            //get the current sms conversation
            SmsConversation currentConv = list.get(i);

            //Link an sms to the playbutton
            view.findViewById(R.id.imageButton).setTag(currentConv.getSmsList().get(0));

<<<<<<< HEAD
            //set the info of the element
            holder.contactName.setText((getContactByNbr(currentConv.getAddress())));
            holder.contactName.setTag(i);
/*
                //view for date
                TextView dateView = (TextView)view.findViewById(R.id.date);
                dateView.setText(currentConv.getSmsList().get(0).getDate());
*/
=======
            //set the correct data of the element
            holder.contactName.setText((getContactByNbr(currentConv.getAddress())));
            holder.contactName.setPaintFlags(holder.contactName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.contactName.setTag(i);
            holder.date.setText(getPropperDate(list.get(i).getLatestDate()));

>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
            return view;
        }
    }

    static class ViewHolder {
        TextView contactName;
<<<<<<< HEAD
=======
        TextView date;
>>>>>>> 7312db6b3e7f3c870d1d248a931e92ddaf774648
    }
}

