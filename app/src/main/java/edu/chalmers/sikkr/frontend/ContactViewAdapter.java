package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.Contact;

/**
 * Adapter that populates a grid view with contacts.
 * 
 * @author Oskar Jönefors
 */
public class ContactViewAdapter extends ArrayAdapter {

    private final Context context;
    private final int layoutResourceId;
    private final List<Contact> contacts;

    public ContactViewAdapter(Context context, int layoutResourceId, List<Contact> contacts) {
        super(context, layoutResourceId, contacts);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.contacts = contacts;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;

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
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.contact_grid_layout);
        layout.setOnClickListener(new ContactGridClickListener(contact));
        holder.contactName.setText(contact.getName());
        holder.image.setImageBitmap(contact.getPhoto());
        return view;
    }

    static class ViewHolder {
        TextView contactName;
        ImageView image;
    }
}