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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.sms.SmsConversation;
import edu.chalmers.sikkr.backend.sms.TheInbox;
import edu.chalmers.sikkr.backend.util.LogUtility;

public class ConversationActivity extends Activity {
    private SmsConversation thisConversation;
    private List<OneSms> messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        createConversationLayout();


    }
    public void createConversationLayout(){
        final Bundle bundle = getIntent().getExtras();
        if(bundle!=null && bundle.containsKey("position") && bundle.containsKey("name")){
            thisConversation = SMS_Activity.getConversations().get(bundle.getInt("position"));
            TextView tv = (TextView)findViewById(R.id.conversation_name);
            tv.setText(bundle.getString("name"));
            messages = thisConversation.getSmsList();
            Collections.sort(messages);
            ArrayAdapter adapter = new ConversationAdapter(this, R.layout.conversationitem_left,messages );
            ListView listV = (ListView)findViewById(R.id.conversation_list);
            listV.setAdapter(adapter);
        }
    }

    public void readMessage(View view){
        LogUtility.writeLogFile("readMessage","Jag kom hit");
        ((OneSms)view.getTag()).play();
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

}
