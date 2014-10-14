package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.calls.OneCall;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;

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
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private void createCallLogLayout() {

        List<OneCall> callList = CallLog.getInstance().getCallList();
        //adapter = en lista av OneCalls i activ
        ArrayAdapter adapter = new LatestCallItemAdapter(this, R.layout.latest_call_item, callList);
        ListView listV = (ListView) findViewById(R.id.listView);
        // tar in en lista av latestcalladaptrar i vyn
        listV.setAdapter(adapter);

    }

    //inner class
    private class LatestCallItemAdapter extends ArrayAdapter {
        private final Context context;
        private final List<OneCall> list;
        private final int layoutId;

        private LatestCallItemAdapter(Context context, int layoutId, List list) {
            super(LatestCallsActivity.this, layoutId, list);
            this.context = context;
            this.list = list;
            this.layoutId = layoutId;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            TextView name;
            TextView date;
            Contact contact;
            String contactID = list.get(i).getContactID();
            Resources res = context.getResources();
            Bitmap bitmap;
            Drawable drawable;
            ImageView image;

            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);

                if (contactID != null) {
                    contact = ContactBook.getSharedInstance().getContact(contactID);

                    //set name to adapter
                    name = (TextView) view.findViewById(R.id.nameText);
                    name.setText(contact.getName());

                    //set pic in the adapter
                    drawable = new BitmapDrawable(getResources(), contact.getPhoto());
                    image = (ImageView) view.findViewById(R.id.latest_call_image);
                    image.setImageDrawable(drawable);
                } else {
                    //setting name as phonenbr
                    name = (TextView) view.findViewById(R.id.nameText);
                    name.setText(list.get(i).getCallNumber());
                }

                switch (list.get(i).getCallType()) {
                    case android.provider.CallLog.Calls.INCOMING_TYPE:
                        bitmap = BitmapFactory.decodeResource(res, res.getIdentifier("incoming_call", "drawable", context.getPackageName()));
                        drawable = new BitmapDrawable(getResources(), bitmap);
                        image = (ImageView) view.findViewById(R.id.call_type);
                        image.setImageDrawable(drawable);
                        break;

                    case android.provider.CallLog.Calls.OUTGOING_TYPE:
                        bitmap = BitmapFactory.decodeResource(res, res.getIdentifier("outgoing_call", "drawable", context.getPackageName()));
                        drawable = new BitmapDrawable(getResources(), bitmap);
                        image = (ImageView) view.findViewById(R.id.call_type);
                        image.setImageDrawable(drawable);
                        break;

                    case android.provider.CallLog.Calls.MISSED_TYPE:
                        name.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }

                //sets the dateView to the correct time
                date = (TextView) view.findViewById(R.id.dateText);
                String callDate = DateDiffUtility.callDateToString(Long.parseLong(list.get(i).getCallDate()));
                date.setText(callDate);
            }

            return view;
        }

    }
 }