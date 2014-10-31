package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.util.DateDiffUtility;

/**
 * Adapter class for the conversations
 * @author Jesper Olsson
 */
public class ConversationAdapter extends ArrayAdapter<ListableMessage> {
    private final Context context;
    private final List<ListableMessage> list;

    public ConversationAdapter(Context context, List list){
        super(context, R.layout.conversationitem_left, list);
        this.context = context;
        this.list = list;
    }

    /**
     * getView method for this adapter. Will change the layout of conversation item
     * depending on wether it's a sent message or recieved messagee
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;
        final ViewHolder holder;
        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.conversationitem_left, parent, false);
            holder = new ViewHolder();
            holder.message = (TextView)view.findViewById(R.id.conversation_message);

            holder.playButton= (Button) view.findViewById(R.id.conversation_icon);
            view.setTag(holder);

        }else{
            holder = (ViewHolder)view.getTag();
        }

        if(list.get(position).isSent()) {
            holder.message.setBackgroundColor(view.getResources().getColor(R.color.light_yellow));

            RelativeLayout.LayoutParams userNameAndChatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameAndChatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams chatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            chatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            RelativeLayout.LayoutParams userNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameParams.addRule(RelativeLayout.LEFT_OF, R.id.conversation_icon);

            holder.playButton.setLayoutParams(chatMessageParams);
            holder.message.setLayoutParams(userNameParams);
        }else {
            holder.message.setBackgroundColor(view.getResources().getColor(R.color.blue));
            RelativeLayout.LayoutParams userNameAndChatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameAndChatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams chatMessageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            chatMessageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            RelativeLayout.LayoutParams userNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userNameParams.addRule(RelativeLayout.RIGHT_OF, R.id.conversation_icon);

            holder.playButton.setLayoutParams(chatMessageParams);
            holder.message.setLayoutParams(userNameParams);
        }
        if(!list.get(position).isRead()){
            holder.playButton.setBackgroundResource(R.drawable.new_message_1);
        }else{
            holder.playButton.setBackgroundResource(R.drawable.old_message_1);
        }
        holder.message.setText(DateDiffUtility.callDateToString(
                list.get(position).getTimestamp().getTimeInMillis(), context));
        holder.playButton.setTag(list.get(position));
        return view;
    }

    /**
     * Class to hold some view elements
     */
    private static class ViewHolder{
        Button playButton;
        TextView message;

    }
}
