package edu.chalmers.sikkr.frontend;

import android.content.Intent;
import android.view.View;

/**
 * A simple class to handle buttons clicked in ContactBookActivity.
 * @author Jesper Olsson
 */
class ContactBookClickListener implements View.OnClickListener {

    private final Character initialLetter;
    public ContactBookClickListener(char initialLetter){
        this.initialLetter = initialLetter;
    }

    /**
     * A method that will perform certain actions when button is clicked.
     * @param view
     */
    public void onClick(View view){
        Intent intent = new Intent(view.getContext(), ContactGridActivity.class);
        intent.putExtra("initial_letter", initialLetter);
        view.getContext().startActivity(intent);
    }

}
