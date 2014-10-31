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

    /**
     * Takes the call list from the CallLog class and sorts them by date and refines the list.
     * Creates the list view layout with each item in the list connected to one call.
     */
    private void createCallLogLayout() {
        List<OneCall> callList = CallLog.getInstance().getCallList();
        Collections.sort(callList);

        List<OneCall> refinedList = this.createRefinedList(callList);

        ArrayAdapter adapter = new LatestCallItemAdapter(this, refinedList);
        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(adapter);
    }

    /**
     * Refines the call list so that calls that were created from the same person and with the same
     * type get sorted as only one call which shows the amount of times it was made. For example
     * if there exist two calls after each other from the same phone number that are all outgoing,
     * then it will be merged as one, showing the number of times it was made and which type it was.
     * @param callList The list that contains all the calls.
     * @return A refined list where calls that are of the same number and that were made after each
     * other gets merged into one call and then setting an amount of them.
     */
    private List<OneCall> createRefinedList (List<OneCall> callList) {
        Map<String, OneCall> map = new LinkedHashMap<>();
        OneCall iCall, jCall;
        String iCallNumber, jCallNumber;
        int iCallType, jCallType;

        for (int i = 0; i < callList.size(); i++) {
            iCall = callList.get(i);
            iCallNumber = MessageUtils.fixNumber(iCall.getCallNumber());
            iCallType = iCall.getCallType();


            if (!map.containsKey(iCallNumber)) {
                iCall.setCallTypeAmount(1);
                map.put(iCallNumber, iCall);
                for (int j = i + 1; j < callList.size(); j++) {
                    jCall = callList.get(j);
                    jCallNumber = MessageUtils.fixNumber(jCall.getCallNumber());
                    jCallType = jCall.getCallType();

                    if (iCallNumber.equals(jCallNumber)
                            && iCallType == jCallType) {
                        iCall.setCallTypeAmount(iCall.getCallTypeAmount() + 1);
                    } else {
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(map.values());
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

        private LatestCallItemAdapter(Context context, List<OneCall> list) {
            super(LatestCallsActivity.this, R.layout.latest_call_item, list);
            this.context = context;
            this.list = list;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            final ViewHolder holder;
            Resources res = context.getResources();
            Contact contact;

            /*
             * If there exists no view, we create one and save all the elements from the view
             * (the view is the latest_call_item) that we need to edit later. Else we reuse the
             * view (this happens when scrolling through the list and scroll back again).
             */
            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(R.layout.latest_call_item, viewGroup, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.contactImage = (ImageView) view.findViewById(R.id.latest_call_image);
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.callTypeAmountAndDate = (TextView) view.findViewById(R.id.callTypeAmountAndDate_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            /*
             * Sets the name text to "private number" if the number is private (all private numbers
             * starts with "-". Else, the number exists and we write either the number itself or
             * the name the number is connected to.
             */
            if(list.get(i).getCallNumber().startsWith("-")) {
                holder.name.setText("Private number");
            } else {
                holder.name.setText(getContactNameByNbr(MessageUtils.fixNumber(list.get(i).getCallNumber())));
            }

            /* Gets the contact by it's name, and if there is no name (i.e only a phone number) then
             * it will return null. This is so we can set the photo if there exists one. Otherwise,
             * everything is set to null to avoid the view to reuse information. Setting listeners
             * where we send either the contact or contact number makes it possible to click on
             * a item in the list and either go to contact information or call the number directly.
             */
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

            /*
             * First we get the difference in date from today and from when the call was made. Then
             * we write the amount of times the call was made if it is bigger than 2 times, and
             * then we write how long ago the call was made.
             */
            String callDate = DateDiffUtility.callDateToString(Long.parseLong( list.get(i).getCallDate()), LatestCallsActivity.this);
            holder.callTypeAmountAndDate.setText
                    (list.get(i).getCallTypeAmount()<2 ? "" + callDate : "(" + list.get(i).getCallTypeAmount() + ")" + "\n" +  callDate);

            /*
             * Here we set the picture accordingly to the call type.
             */
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
         * Get the saved contact name related to the number from the phonebook, if there is no
         * contact related to the number (i.e it's a unsaved phone number), then it will return
         * the number itself.
         * @param number The number to get the contact name from.
         * @return The name of the contact or the number if there exist no name.
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