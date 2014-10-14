package edu.chalmers.sikkr.frontend;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.ContactBook;

/**
 * A class to create the Button Grid in ContactBookActivity.
 * @author Jesper Olsson
 */
public class ButtonAdapter extends BaseAdapter {
    final private Context mContext;
    private Character mLetter;
    final private ArrayList<Character> al;

    public ButtonAdapter(Context c) {
        mContext = c;
        al = new ArrayList<Character>();
        al.addAll(ContactBook.getSharedInstance().getInitialLetters());

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
     * @param position the position of the button in the grid
     * @param convertView
     * @param parent
     * @return the view of the item
     */
    public View getView (int position, View convertView, ViewGroup parent){
        Button btn;
        if(convertView == null) {
            btn = new Button(mContext);
            btn.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT,500 ));

        }else{
            btn = (Button) convertView;
        }
        btn.setText(String.valueOf(Character.toUpperCase((al.get(position)))));
        btn.setTextColor(Color.BLACK);
        btn.setTextSize(80);
        btn.setBackground(null);
        btn.setId(position);

        btn.setOnClickListener(new ContactBookClickListener(position, btn.getText().charAt(0)));

        return btn;
    }





}
