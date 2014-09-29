package edu.chalmers.sikkr;

import android.content.Intent;
import android.view.View;

import edu.chalmers.sikkr.backend.Contact;

/**
 * Created by ivaldi on 2014-09-29.
 */
public class ContactGridClickListener implements View.OnClickListener {

    private Contact contact;

    public ContactGridClickListener(Contact contact) {
        this.contact = contact;
    }

    @Override
    public void onClick(View v) {
        final Intent intent = new Intent(v.getContext(), ContactActivity.class);
        intent.putExtra("contact_id", contact.getID());
        v.getContext().startActivity(intent);
    }
}
