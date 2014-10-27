package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_latest_calls);
        createCallLogLayout();
    }


    private void createCallLogLayout() {

        List<OneCall> callList = CallLog.getInstance().getCallList();
        Collections.sort(callList);
        ArrayAdapter adapter = new LatestCallItemAdapter(this, R.layout.latest_call_item, callList);
        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(adapter);
    }

    private static class ViewHolder {
        TextView name;
        TextView date;
        Contact contact;
        Bitmap bitmap;
        Drawable drawable;
        Drawable contactDrawable;
        ImageView contactImage;
        ImageView image;
    }

    private class LatestCallItemAdapter extends ArrayAdapter {
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
            String contactID = list.get(i).getContactID();

            if (view == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(layoutId, viewGroup, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.contactImage = (ImageView) view.findViewById(R.id.latest_call_image);
                holder.name = (TextView) view.findViewById(R.id.nameText);
                holder.date = (TextView) view.findViewById(R.id.dateText);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (contactID != null && ContactBook.getSharedInstance().getContact(contactID) != null) {
                holder.contact = ContactBook.getSharedInstance().getContact(contactID);
                holder.name.setText(holder.contact.getName());

                if (holder.contact.getPhoto() != null) {
                    holder.contactDrawable = new BitmapDrawable(getResources(), holder.contact.getPhoto());
                    holder.contactImage.setImageDrawable(holder.contactDrawable);
                }

                view.setOnClickListener(new ContactGridClickListener(holder.contact));

            } else {
                holder.name.setText(list.get(i).getCallNumber());
                holder.contactDrawable = null;
                holder.contactImage.setImageDrawable(null);
            }

            String callDate = DateDiffUtility.callDateToString(Long.parseLong(list.get(i).getCallDate()));
            holder.date.setText(callDate);

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
                    holder.name.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    break;
                // måste kolla färg varje gång
            }
            return view;
        }
    }
}