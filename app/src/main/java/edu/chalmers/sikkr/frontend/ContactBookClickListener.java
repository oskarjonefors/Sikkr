package edu.chalmers.sikkr.frontend;

import android.content.Intent;
import android.view.View;

import edu.chalmers.sikkr.frontend.ContactGridActivity;

/**
 * Created by Jesper on 2014-09-28.
 */
class ContactBookClickListener implements View.OnClickListener {

    final private int position;
    private Character initialLetter;
    public ContactBookClickListener(int position, Character initialLetter){

        this.position = position;
        this.initialLetter = initialLetter;
    }

    public void onClick(View view){
        Intent intent = new Intent(view.getContext(), ContactGridActivity.class);
        intent.putExtra("initial_letter", initialLetter);
        view.getContext().startActivity(intent);
    }

}
