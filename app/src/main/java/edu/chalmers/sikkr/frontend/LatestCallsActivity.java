package edu.chalmers.sikkr.frontend;

import android.app.Activity;
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.calls.OneCall;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;

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
        private Calendar calendar;

        private LatestCallItemAdapter(Context context, int layoutId, List list) {
            super(LatestCallsActivity.this, layoutId, list);
            this.context = context;
            this.list = list;
            this.layoutId = layoutId;
            calendar = GregorianCalendar.getInstance();
        }

        //iterar genom denna metod pga listan


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            TextView name;
            TextView date;
            Contact contact;
            String contactID = list.get(i).getContactID();



            //get the contact from List<OneCall>

            if(view==null){
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);

                if(contactID!=null) {
                    contact = ContactBook.getSharedInstance().getContact(contactID);

                    //set name to adapter
                    name = (TextView) view.findViewById(R.id.nameText);
                    name.setText(contact.getName());

                    //set pic in the adapter
                    Drawable d = new BitmapDrawable(getResources(), contact.getPhoto());
                    ImageView image = (ImageView) view.findViewById(R.id.latest_call_image);
                    image.setImageDrawable(d);
                }
                else {
                    //setting name as phonenbr
                    name = (TextView) view.findViewById(R.id.nameText);
                    name.setText(list.get(i).getCallNumber());
                }
                    //sets the correct timezone of to the calendar
                    calendar = millisToDate(Long.parseLong(list.get(i).getCallDate()));

                    //sets the dateView to the correct time
                    date = (TextView) view.findViewById(R.id.dateText);
                    date.setText(callDateToString(calendar));
            }

            return view;
        }
        //converts the calldate given in milliseconds and returns a calendar in the correct Timezone.
        public Calendar millisToDate(Long millis){

            //Sets the current and the desired timezone
            //TimeZone fromTZ = calendar.getTimeZone();
            TimeZone toTz = TimeZone.getDefault();

            //Set a date in calendar
            calendar.setTimeInMillis(millis);
            //UTC to timezone
           calendar.add(Calendar.MILLISECOND, toTz.getRawOffset());

            //Checks the daylight savings (summertime or not)
            if (toTz.inDaylightTime(calendar.getTime())) {
                calendar.add(Calendar.MILLISECOND, toTz.getDSTSavings());
            }

            return calendar;
        }

        public String callDateToString(Calendar calendar) {

            Calendar rightNow = GregorianCalendar.getInstance();

            long callDateMillis = calendar.getTimeInMillis();
            long rightNowMillis = rightNow.getTimeInMillis();

            long deltaMillis = rightNowMillis - callDateMillis;

            int timeDays = (int) TimeUnit.DAYS.convert(deltaMillis, TimeUnit.DAYS);
            int timeHours = (int) TimeUnit.HOURS.convert(deltaMillis, TimeUnit.HOURS);
            int timeMinutes= (int) TimeUnit.MINUTES.convert(deltaMillis, TimeUnit.MINUTES);

            Log.i("TD, TH, TM"," "+ timeDays + " " + timeHours + " " + timeMinutes);

            if ((timeDays /7 ) >= 1) {

                if ((timeDays/7) <= 4) {
                    return (timeDays/7) + " w";
                }

            } else if (timeDays >= 1) {

                return (timeDays / 1) + " d";

            } else if ((timeHours / 1) > 0) {

                return timeHours + " h";

            } else if ((timeMinutes / 5) >= 1) {

                return timeMinutes + " min";

            }

            return "Just Now";
        }
    }
 }