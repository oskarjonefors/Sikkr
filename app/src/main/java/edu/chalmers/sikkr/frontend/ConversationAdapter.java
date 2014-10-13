package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        if(list.get(position).isSent()){
            holder.message.setBackgroundColor(Color.BLUE);
        }else{
            holder.message.setBackgroundColor(Color.GREEN);
        }

        view.findViewById(R.id.conversation_icon).setTag(list.get(position));
        holder.message.setText(list.get(position).getMessage());
        return view;
    }

    private static class ViewHolder{
        TextView message;

    }
}
