package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ListableMessage;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;

public class SMS_Activity extends Activity {
    private static ArrayList<SmsConversation> smsList;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_layout);
        createSmsLayout();
        BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    String messageBody = smsMessage.getMessageBody();
                    String phoneNbr = smsMessage.getOriginatingAddress();
                    String date = String.valueOf(smsMessage.getTimestampMillis());
                    OneSms sms = new OneSms(messageBody, phoneNbr, date, false);
                    for(int i = 0;i<smsList.size();i++){
                        if(phoneNbr.equals(smsList.get(i).getAddress())){
                            sms.markAsUnread();
                            smsList.get(i).addSms(sms);
                        }
                    }
                }
                adapter.setNotifyOnChange(true);
                adapter.notifyDataSetChanged();
            }
        };
        registerReceiver(broadcastReciever, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }


    private void createSmsLayout() {
        smsList = TheInbox.getInstance().getSmsInbox();
        adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
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

    public static ArrayList<SmsConversation> getConversations(){
        return smsList;
    }

    public void readMsg(View view) {
        ((ListableMessage) view.getTag()).play();
        ((OneSms)view.getTag()).markAsRead();
        ImageButton tryButton = (ImageButton)view.findViewById(R.id.imageButton);
        tryButton.setBackgroundResource(R.drawable.play);
    }

    /**
     *
    Open the clicked contacts sms history view
     */
    public void clickedText(View view) {
        int position = (Integer) view.getTag();
        Intent intent = new Intent(view.getContext(), ConversationActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("name", getContactByNbr(smsList.get(position).getAddress()));
        startActivity(intent);
    }

    /**
     * Get the saved contact name related to the number from the phonebook
     * @param number
     * @return
     */
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
            } catch(Exception e){}
            finally {
                cursor.close();
            }
        }
        if(contact.length() == 0)
            return number;
        return contact;
    }

    public static String getPropperDate(String s) {
        Long dateNbr = Long.parseLong(s);
        Date date = new Date(dateNbr);
        return new SimpleDateFormat("EEE, MMM d, ''yy").format(date);

    }

    //Inner adapter class
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
                holder.date = (TextView) view.findViewById(R.id.date);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            //get the current sms conversation
            SmsConversation currentConv = list.get(i);
            Set<ListableMessage> messageSet = currentConv.getSmsList();
            List<ListableMessage> messageList = new ArrayList<ListableMessage>();
            messageList.addAll(messageSet);
            Collections.sort(messageList);
            ImageButton tryButton = (ImageButton)view.findViewById(R.id.imageButton);
            //Link an sms to the playbutton
            int counter = 0;
            while(messageList.get(counter).isSent()){
                counter = counter + 1;
            }
            if(!messageList.get(counter).isRead()){
                tryButton.setBackgroundResource(R.drawable.unread_play);
            }
            view.findViewById(R.id.imageButton).setTag(messageList.get(counter));
            LogUtility.writeLogFile("counterweird", "Counter = " +counter);

            //set the correct data of the element
            holder.contactName.setText((getContactByNbr(currentConv.getAddress())));
            holder.contactName.setPaintFlags(holder.contactName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.contactName.setTag(i);
            holder.date.setText(getPropperDate(list.get(i).getLatestDate()));


            return view;
        }
    }

    static class ViewHolder {
        TextView contactName;
        TextView date;
    }
}

