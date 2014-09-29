package edu.chalmers.sikkr;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import edu.chalmers.sikkr.backend.Contact;

/**
 * Created by ivaldi on 2014-09-24.
 */
public class ContactViewAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private List<Contact> contacts;

    public ContactViewAdapter(Context context, int layoutResourceId, List<Contact> contacts) {
        super(context, layoutResourceId, contacts);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.contacts = contacts;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;

        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.contactName = (TextView)view.findViewById(R.id.contact_name);
            holder.image = (ImageView)view.findViewById(R.id.contact_thumb);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Contact contact = contacts.get(position);
        holder.contactName.setText(contact.getName());
        holder.image.setImageBitmap(contact.getPhoto());
        Log.d("ContactViewAdapter", "SETTING IMAGE " + contact.getPhoto() + "for " + contact.getName());
        return view;
    }

    static class ViewHolder {
        TextView contactName;
        ImageView image;
    }
}