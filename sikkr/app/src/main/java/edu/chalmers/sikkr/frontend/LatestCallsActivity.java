package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.calls.CallLog;
import edu.chalmers.sikkr.backend.calls.OneCall;
import edu.chalmers.sikkr.backend.contact.Contact;
import edu.chalmers.sikkr.backend.contact.ContactBook;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;
import edu.chalmers.sikkr.backend.util.LogUtility;
import edu.chalmers.sikkr.backend.util.MessageUtils;

public class LatestCallsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_latest_calls);
        createCallLogLayout();
    }


    private List<OneCall> createRefinedList (List<OneCall> callList){
        Map <String, OneCall > map = new LinkedHashMap<>();
        OneCall iCall, jCall;
        String iCallNumber, jCallNumber;
        int iCallType, jCallType;

        for (int i = 0; i < callList.size(); i++) {
            iCall = callList.get(i);
            iCallNumber = MessageUtils.fixNumber(iCall.getCallNumber());
            iCallType = iCall.getCallType();
            LogUtility.writeLogFile("CreateRefinedList", "Found number " + iCallNumber);

            if (!map.containsKey(iCallNumber)) {
                iCall.setCallTypeAmount(1); //Sätt till ett eftersom det är första gången numret uppkommer
                map.put(iCallNumber, iCall); //Lägg till nummer eftersom det är första gången det uppkommer
                LogUtility.writeLogFile("CreateRefinedList", "Adding new number to latest calls: " + iCallNumber);
                for (int j = i + 1; j < callList.size(); j++) {
                    jCall = callList.get(j);
                    jCallNumber = MessageUtils.fixNumber(jCall.getCallNumber());
                    jCallType = jCall.getCallType();

                    LogUtility.writeLogFile("CreateRefinedList", "Testing number " + iCallNumber
                            + " with type " + iCallType + " against " + jCallNumber + " with type "
                            + jCallType);
                    if (iCallNumber.equals(jCallNumber)
                            && iCall.getCallType() == jCallType) {
                        iCall.setCallTypeAmount(iCall.getCallTypeAmount() + 1);
                    } else {
                        break;
                    }
                }
            } else {
                LogUtility.writeLogFile("CreateRefinedList", "The number was already in the list");
            }
        }

        return new ArrayList<>(map.values());

    }

    private void createCallLogLayout() {

        List<OneCall> callList = CallLog.getInstance().getCallList();
        Collections.sort(callList);

        List<OneCall> refinedList = this.createRefinedList(callList);

        ArrayAdapter adapter = new LatestCallItemAdapter(this, R.layout.latest_call_item, refinedList);
        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(adapter);
    }

    private static class ViewHolder {
        TextView name;
        TextView callTypeAmountAndDate;
        Contact contact;
        Bitmap bitmap;
        Drawable drawable;
        Drawable contactDrawable;
        ImageView contactImage;
        ImageView image;
    }

    private class LatestCallItemAdapter extends ArrayAdapter<OneCall> {
        private final Context context;
        private final List<OneCall> list;
        private final int layoutId;

        private LatestCallItemAdapter(Context context, int layoutId, List<OneCall> list) {
            super(LatestCallsActivity.this, layoutId, list);
            this.context = context;
            this.list = list;
            this.layoutId = layoutId;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            final ViewHolder holder;
            Resources res = context.getResources();
            Contact contact;

            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.contactImage = (ImageView) view.findViewById(R.id.latest_call_image);
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.callTypeAmountAndDate = (TextView) view.findViewById(R.id.callTypeAmountAndDate_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if(list.get(i).getCallNumber().startsWith("-")) {
                holder.name.setText("Private number");
            } else {
                holder.name.setText(getContactNameByNbr(MessageUtils.fixNumber(list.get(i).getCallNumber())));
            }

            contact = ContactBook.getSharedInstance().getFirstContactByName((holder.name.getText() + "").trim());
            if (contact != null) {
                holder.contact = contact;
                if (contact.getPhoto() != null) {
                    holder.contactDrawable = new BitmapDrawable(getResources(), holder.contact.getPhoto());
                    holder.contactImage.setImageDrawable(holder.contactDrawable);
                } else {
                    holder.contactDrawable = null;
                    holder.contactImage.setImageDrawable(null);
                }
                view.setOnClickListener(new ContactGridClickListener(contact));
            } else {
                holder.contact = null;
                holder.contactDrawable = null;
                holder.contactImage.setImageDrawable(null);
                view.setOnClickListener(new ContactGridClickListener(list.get(i).getCallNumber()));
            }

            String callDate = DateDiffUtility.callDateToString(Long.parseLong( list.get(i).getCallDate()), LatestCallsActivity.this);
            holder.callTypeAmountAndDate.setText
                    (list.get(i).getCallTypeAmount()<2 ? "" + callDate : "(" + list.get(i).getCallTypeAmount() + ")" + "\n" +  callDate);

            switch (list.get(i).getCallType()) {
                case android.provider.CallLog.Calls.INCOMING_TYPE:
                    holder.bitmap = BitmapFactory.decodeResource(res, res.getIdentifier("incoming_call", "drawable", context.getPackageName()));
                    holder.drawable = new BitmapDrawable(getResources(), holder.bitmap);
                    holder.image = (ImageView) view.findViewById(R.id.call_type);
                    holder.image.setImageDrawable(holder.drawable);
                    holder.name.setTextColor(getResources().getColor(android.R.color.black));
                    break;

                case android.provider.CallLog.Calls.OUTGOING_TYPE:
                    holder.bitmap = BitmapFactory.decodeResource(res, res.getIdentifier("outgoing_call", "drawable", context.getPackageName()));
                    holder.drawable = new BitmapDrawable(getResources(), holder.bitmap);
                    holder.image = (ImageView) view.findViewById(R.id.call_type);
                    holder.image.setImageDrawable(holder.drawable);
                    holder.name.setTextColor(getResources().getColor(android.R.color.black));
                    break;

                case android.provider.CallLog.Calls.MISSED_TYPE:
                    holder.bitmap = BitmapFactory.decodeResource(res, res.getIdentifier("missed_call", "drawable", context.getPackageName()));
                    holder.drawable = new BitmapDrawable(getResources(), holder.bitmap);
                    holder.image = (ImageView) view.findViewById(R.id.call_type);
                    holder.image.setImageDrawable(holder.drawable);
                    holder.name.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    break;

            }
            return view;
        }

        /**
         * Get the saved contact name related to the number from the phonebook
         * @param number the number to get the contact name from
         * @return the name of the contact
         */
        public String getContactNameByNbr(String number) {
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
                    LogUtility.writeLogFile("getting_contacts_log", e, LatestCallsActivity.this);
                }
                finally {
                    cursor.close();
                }
            }
            if(contact.length() == 0)
                return number;
            return contact;
        }
    }
}