package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;

public class ConversationActivity extends Activity {
    private ArrayList<SmsConversation> smsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        createConversationLayout();


    }
    public void createConversationLayout(){
        smsList = TheInbox.getInstance().getSmsInbox();
        ArrayAdapter adapter = new ConversationAdapter(this, R.layout.conversationitem_left, smsList);
        ListView listV = (ListView)findViewById(R.id.conversation_list);
        listV.setAdapter(adapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation, menu);
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

    private class ConversationAdapter extends ArrayAdapter {
        private final Context mContext;
        private final List<SmsConversation> mList;
        private final int mLayoutId;
        private String name ="";



        private ConversationAdapter(Context context, int layoutId, List list) {
            super(ConversationActivity.this, layoutId, list);
            mContext = context;
            mList = list;
            mLayoutId = layoutId;
            final Bundle bundle = getIntent().getExtras();
            if(bundle!=null && bundle.containsKey("contactName")){
                name = bundle.getString("contactName");
            }

        }
        public View getView(int i, final View v, ViewGroup viewGroup) {
            View view = v;
            final ViewHolder holder;
            if (view == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                view = inflater.inflate(mLayoutId, viewGroup, false);
                holder = new ViewHolder();
                //holder.contactName = (TextView) view.findViewById(R.id.ContName);
                view.setTag(holder);
            } else {
                holder = (ViewHolder)view.getTag();
            }
            TextView tv = (TextView)view.findViewById(R.id.ContName);
            tv.setText(name);
            //holder.contactName.setText(name);
            return view;
        }
    }

    static class ViewHolder{
        TextView contactName;
    }
}
