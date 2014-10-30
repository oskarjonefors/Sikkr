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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.messages.Conversation;
import edu.chalmers.sikkr.backend.messages.InboxDoneLoadingListener;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.messages.OneSms;
import edu.chalmers.sikkr.backend.messages.TheInbox;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.ServerInterface;
import edu.chalmers.sikkr.backend.util.VoiceMessageSender;


/**
 * Activity for showing the sms inbox
 */

public class SMS_Activity extends Activity implements InboxDoneLoadingListener {
    private static List<Conversation> smsList;
    private SmsViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServerInterface.addSingletonInboxDoneLoadingListener(this);
        VoiceMessageSender.addInboxDoneLoadingListener(this);
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
                    OneSms sms = new OneSms(messageBody, date, false);
                    List<Conversation> list = TheInbox.getInstance().getMessageInbox();

                    for (Conversation conversation : list) {
                        if (phoneNbr.equals(conversation.getAddress())) {
                            sms.markAsUnread();
                            conversation.addMessage(sms);
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
            LogUtility.writeLogFile("Loaded_message_inbox_activity", "Loading the message inbox list");
            smsList = TheInbox.getInstance().getMessageInbox();
            LogUtility.writeLogFile("Loaded_message_inbox_activity", "Loaded the message inbox list");
            findViewById(R.id.inboxProgressBar).setVisibility(View.GONE);
            LogUtility.writeLogFile("Loaded_message_inbox_activity", "Removed the progressbar");
            LogUtility.writeLogFile("smLsiT", "" + smsList.size());
            adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
            ListView listV = (ListView) findViewById(R.id.listView);
            listV.setAdapter(adapter);
        } catch (Exception e) {
            LogUtility.writeLogFile("load_inbox_throws", e, this);
        }
    }

    /**
     * OnClick for play button
     * Will play the latest recieved message
     * @param view the view that called this method
     */
    public void readMsg(View view) {
        try {
            ListableMessage message = (ListableMessage) view.getTag();
            Button tryButton = (Button)view.findViewById(R.id.imageButton);
            message.play(new PlayButtonHandler(tryButton, message, this));
            message.markAsRead();
        } catch (Exception e) {
            LogUtility.writeLogFile("ReadMessageLogs", e, this);
        }
    }

    /**
     *
    Open the clicked contacts sms history view
     */
    public void clickedText(View view) {
        try {
            final int position = (Integer) view.getTag();
            final Conversation conversation = smsList.get(position);
            final String address = conversation.hasLocalNumber() ? conversation.getAddress()
                    : conversation.getFixedNumber();

            Intent intent = new Intent(view.getContext(), ConversationActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("name", getContactByNbr(address));
            intent.putExtra("number", conversation.getFixedNumber());
            startActivity(intent);
        } catch (Exception e) {
            LogUtility.writeLogFile("ClickedConversation", e, this);
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

        if (cursor != null && cursor.getCount() > 0) {
            try {
                cursor.moveToNext();
                contact = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            } catch(Exception e){
                LogUtility.writeLogFile("getting_contacts_log", e, this);
            }
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
        try {
            adapter.notifyDataSetChanged();
        } catch (Throwable t) {
            LogUtility.writeLogFile("Loaded_message_inbox_activity", t, this);
        }
    }

    //Inner adapter class
    public class SmsViewAdapter extends ArrayAdapter<Conversation> {

        private final Context context;
        private final List<Conversation> list;
        private final int layoutId;

        private SmsViewAdapter(Context context, int layoutId, List<Conversation> list) {
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
                List<ListableMessage> messageList = new ArrayList<>();
                messageList.addAll(messageSet);
                Collections.sort(messageList);
                Button tryButton = (Button)view.findViewById(R.id.imageButton);

                //Link an sms to the playbutton
                int counter = messageList.size() -1;

                while(messageList.get(counter).isSent() && counter >= 1){
                    counter--;
                }
                if(!messageList.get(counter).isRead()){
                    tryButton.setBackgroundResource(R.drawable.new_message_1);
                }else{
                    tryButton.setBackgroundResource(R.drawable.old_message_1);
                }
                view.findViewById(R.id.imageButton).setTag(messageList.get(counter));
                String address = currentConv.hasLocalNumber() ? currentConv.getAddress()
                        : currentConv.getFixedNumber();
                //set the correct data of the element
                holder.contactName.setText(getContactByNbr(address));
                holder.contactName.setPaintFlags(holder.contactName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.contactName.setTag(i);

                Object[] messages = list.get(i).getSmsList().toArray();
                holder.date.setText(DateDiffUtility.callDateToString(((ListableMessage) messages[messages.length - 1]).getTimestamp().getTimeInMillis()));
            } catch (Exception e) {
                LogUtility.writeLogFile("SmsViewAdapterLogs", e, SMS_Activity.this);
            }

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            Collections.sort(list, new LatestDateComparator());
            super.notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        TextView contactName;
        TextView date;

    }

    class LatestDateComparator implements Comparator<Conversation> {

        @Override
        public int compare(Conversation conversation, Conversation conversation2) {
            final Calendar cal1 = conversation.getLatestMessage().getTimestamp();
            final Calendar cal2 = conversation2.getLatestMessage().getTimestamp();
            return cal2.compareTo(cal1);
        }
    }
}

