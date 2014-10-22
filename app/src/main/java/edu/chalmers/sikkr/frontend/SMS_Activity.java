package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.SmsListener;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.messages.InboxDoneLoadingListener;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.messages.Conversation;
import edu.chalmers.sikkr.backend.messages.TheInbox;


/**
 * Activity for showing the sms inbox
 */

public class SMS_Activity extends Activity implements InboxDoneLoadingListener {
    private static List<Conversation> smsList;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
                    ArrayList<SmsConversation> list = TheInbox.getInstance().getSmsInbox();

                    for (int i = 0; i < list.size(); i++) {
                        if (phoneNbr.equals(list.get(i).getAddress())) {
                            sms.markAsUnread();
                            list.get(i).addSms(sms);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                adapter.setNotifyOnChange(true);

            }
        };

        registerReceiver(broadcastReciever, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }

    private void createSmsLayout() {
        try {
            TheInbox.getInstance().loadInbox(this);
        } catch (Exception e) {
            LogUtility.writeLogFile("load_inbox_throws", e);
        }
    }

    //public static ArrayList<SmsConversation> getConversations(){
        //return smsList;
    //}

    public void readMsg(View view) {
        try {
            ListableMessage message = (ListableMessage) view.getTag();
            message.play();
            message.markAsRead();
            ImageButton tryButton = (ImageButton)view.findViewById(R.id.imageButton);
            tryButton.setBackgroundResource(R.drawable.play);
        } catch (Exception e) {
            LogUtility.writeLogFile("ReadMessageLogs", e);
        }
    }

    /**
     *
    Open the clicked contacts sms history view
     */
    public void clickedText(View view) {
        try {
            int position = (Integer) view.getTag();
            Intent intent = new Intent(view.getContext(), ConversationActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("name", getContactByNbr(smsList.get(position).getAddress()));
            intent.putExtra("number", smsList.get(position).getAddress());
            startActivity(intent);
        } catch (Exception e) {
            LogUtility.writeLogFile("ClickedConversation", e);
        }
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

    @Override
    public void onDone() {
        smsList = TheInbox.getInstance().getMessageInbox();
        findViewById(R.id.inboxProgressBar).setVisibility(View.GONE);
        LogUtility.writeLogFile("smLsiT", ""+smsList.size());
        adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(adapter);
    }

    //Inner adapter class
    public class SmsViewAdapter extends ArrayAdapter {

        private final Context context;
        private final List<Conversation> list;
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

            try {
                final ViewHolder holder;

                if (i < 0 || i >= list.size()) {
                    return null;
                }

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
                Conversation currentConv = list.get(i);
                Set<ListableMessage> messageSet = currentConv.getSmsList();
                List<ListableMessage> messageList = new ArrayList<ListableMessage>();
                messageList.addAll(messageSet);
                Collections.sort(messageList);
                ImageButton tryButton = (ImageButton)view.findViewById(R.id.imageButton);

                //Link an sms to the playbutton
                int counter = messageList.size() -1;

                while(messageList.get(counter).isSent()){
                    counter = counter - 1;
                }
                if(!messageList.get(counter).isRead()){
                    tryButton.setBackgroundResource(R.drawable.unread_play);
                }else{
                    tryButton.setBackgroundResource(R.drawable.play);
                }
                view.findViewById(R.id.imageButton).setTag(messageList.get(counter));
                LogUtility.writeLogFile("counterweird", "Counter = " + counter);

                //set the correct data of the element
                holder.contactName.setText((getContactByNbr(currentConv.getAddress())));
                holder.contactName.setPaintFlags(holder.contactName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.contactName.setTag(i);
                holder.date.setText(DateDiffUtility.callDateToString(((ListableMessage[]) list.get(i).getSmsList().toArray())[0].getTimestamp().getTimeInMillis()));
            } catch (Exception e) {
                LogUtility.writeLogFile("SmsViewAdapterLogs", e);
            }

            return view;
        }
    }

    static class ViewHolder {
        TextView contactName;
        TextView date;

    }
}

