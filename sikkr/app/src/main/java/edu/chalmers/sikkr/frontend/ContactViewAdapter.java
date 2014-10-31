package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.contact.Contact;

/**
 * Adapter that populates a grid view with contacts.
 * 
 * @author Oskar Jönefors
 */
public class ContactViewAdapter extends ArrayAdapter<Contact> {

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
            holder.displayPic = (Button)view.findViewById(R.id.contact_thumb);
            holder.star = (ImageView)view.findViewById(R.id.fav_star);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Contact contact = contacts.get(position);
        holder.displayPic.setOnClickListener(new ContactGridClickListener(contact));
        holder.contactName.setText(contact.getName());
        holder.displayPic.setBackground(new BitmapDrawable(context.getResources(), contact.getPhoto()));
        if (contact.isFavorite()) {
            holder.star.setVisibility(View.VISIBLE);
        }
        return view;
    }

    static class ViewHolder {
        TextView contactName;
        Button displayPic;
        ImageView star;
    }
}