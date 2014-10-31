package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import edu.chalmers.sikkr.backend.contact.Contact;

/**
 * @author Oskar Jönefors
 */
public class ContactGridClickListener implements View.OnClickListener {

    private final Contact contact;
    private final String number;

    public ContactGridClickListener(Contact contact) {
        this.contact = contact;
        this.number = null;
    }

    public ContactGridClickListener(String number) {
        this.contact = null;
        this.number = number;
    }

    @Override
    public void onClick(View view) {
        if (contact != null) {
            openContactActivity(view.getContext());
        } else if (number != null) {
            callNumber(number, (Activity) view.getContext());
        }

    }

    public void openContactActivity(Context context) {
        final Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra("contact_id", contact.getID());
        context.startActivity(intent);
    }

    public void callNumber(String number, Activity activity) {
        //Brings out the phone dialer
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);

        //Sets the data for which number to call
        phoneIntent.setData(Uri.parse("tel:" + number));
        try {
            activity.startActivity(phoneIntent);
            activity.finish();
        } catch (ActivityNotFoundException e) {
            Log.e("Exception ocurred, could not make a call", "");
        }
    }
}
