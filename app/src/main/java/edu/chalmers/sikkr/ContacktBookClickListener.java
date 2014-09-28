package edu.chalmers.sikkr;

import android.content.Intent;
import android.view.View;

/**
 * Created by Jesper on 2014-09-28.
 */
class ContactBookClickListener implements View.OnClickListener {

    final private int position;

    public ContactBookClickListener(int position){
        this.position = position;
    }

    public void onClick(View view){
    Intent intent = new Intent(view.getContext(), ContactGridActivity.class);
    view.getContext().startActivity(intent);

    }

}
