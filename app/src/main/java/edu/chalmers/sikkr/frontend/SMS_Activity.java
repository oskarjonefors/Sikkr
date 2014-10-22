package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.util.HashList;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ListableMessage;
import edu.chalmers.sikkr.backend.SmsListener;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;
import edu.chalmers.sikkr.backend.util.LogUtility;

public class SMS_Activity extends Activity {
    private static ArrayList<SmsConversation> smsList;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_layout);
        createSmsLayout();
        BroadcastReceiver receiver = new SmsListener(this);
    }

    private void createSmsLayout() {
        smsList = TheInbox.getInstance().getSmsInbox();
        LogUtility.writeLogFile("smLsiT", ""+smsList.size());
        adapter = new SmsViewAdapter(this, R.layout.sms_item, smsList);
        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(adapter);
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
            int counter = messageList.size() -1;

            while(messageList.get(counter).isSent()){
                counter = counter - 1;
            }
            if(!messageList.get(counter).isRead()){
                tryButton.setBackgroundResource(R.drawable.unread_play);
            }else{
                tryButton.setBackgroundResource(R.drawable.play);
            }

            //Link an sms to the playbutton
            view.findViewById(R.id.imageButton).setTag(messageList.get(counter));

            //set the correct data of the element
            holder.contactName.setText((getContactByNbr(currentConv.getAddress())));
            holder.contactName.setPaintFlags(holder.contactName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.contactName.setTag(i);
            holder.date.setText(DateDiffUtility.callDateToString(Long.parseLong(list.get(i).getLatestDate())));

            return view;
        }
    }

    static class ViewHolder {
        TextView contactName;
        TextView date;

    }
}

