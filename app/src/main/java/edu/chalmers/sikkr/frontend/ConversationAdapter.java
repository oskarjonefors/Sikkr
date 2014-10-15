package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.sms.OneSms;
import edu.chalmers.sikkr.backend.util.LogUtility;

/**
 * Created by Jesper on 2014-10-13.
 */
public class ConversationAdapter extends ArrayAdapter {
    private Context context;
    private int layoutId;
    private List<OneSms> list;

    public ConversationAdapter(Context context, int layoutId, List list){
        super(context, layoutId, list);
        this.context = context;
        this.layoutId = layoutId;
        this.list = list;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LogUtility.writeLogFile("adapter", "Kom till adaptern");
        View view = convertView;
        final ViewHolder holder;
        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutId, parent, false);
            holder = new ViewHolder();
            holder.message = (TextView)view.findViewById(R.id.conversation_message);
            holder.playButton=(ImageButton)view.findViewById(R.id.conversation_icon);
            view.setTag(holder);

        }else{
            holder = (ViewHolder)view.getTag();
        }

        if(list.get(position).isSent()) {
            holder.message.setBackgroundColor(Color.BLUE);

            RelativeLayout.LayoutParams userNameAndChatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameAndChatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams chatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            chatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            RelativeLayout.LayoutParams userNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameParams.addRule(RelativeLayout.LEFT_OF, R.id.conversation_icon);

            holder.playButton.setLayoutParams(chatMessageParams);
            holder.message.setLayoutParams(userNameParams);
        }else {
            holder.message.setBackgroundColor(Color.GREEN);
            RelativeLayout.LayoutParams userNameAndChatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameAndChatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams chatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            chatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            RelativeLayout.LayoutParams userNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameParams.addRule(RelativeLayout.RIGHT_OF, R.id.conversation_icon);

            holder.playButton.setLayoutParams(chatMessageParams);
            holder.message.setLayoutParams(userNameParams);
        }
        holder.message.setText(new SimpleDateFormat("EEE, MMM d, ''yy").format(list.get(position).getTimestamp().getTime()));
        holder.playButton.setTag(list.get(position));
        return view;
    }

    private static class ViewHolder{
        ImageButton playButton;
        TextView message;

    }
}
