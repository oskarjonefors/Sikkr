package edu.chalmers.sikkr;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.ContactBook;

/**
 * @author Jesper Olsson
 */
public class ButtonAdapter extends BaseAdapter {
    private Context mContext;
    private ContactBook cb;
    final private ArrayList<Character> al;

    public ButtonAdapter(Context c) {
        mContext = c;
        cb = new ContactBook(mContext);
        al = new ArrayList<Character>();
        al.addAll(cb.getInitialLetters());

    }
    /**
     * @return the number of items the grid shall contain
     */
    public int getCount(){
        return al.size();
    }

    public Object getItem(int position){
        return null;
    }

    public long getItemId(int position){
        return position;
    }

    /**
     * Designing the items that will be added to the grid
     * @param position
     * @param convertView
     * @param parent
     * @return the view of the item
     */
    public View getView (int position, View convertView, ViewGroup parent){
        Button btn;
        if(convertView == null) {
            btn = new Button(mContext);
            btn.setLayoutParams(new GridView.LayoutParams(200, 200));
            btn.setPadding(8,8,8,8);
        }else{
            btn = (Button) convertView;
        }
        btn.setText(String.valueOf(al.get(position)));
        btn.setTextColor(Color.BLACK);
        btn.setTextSize(50);
        btn.setBackgroundResource(R.drawable.background);
        btn.setId(position);

        btn.setOnClickListener(new ContactBookClickListener(position));

        return btn;
    }



}
