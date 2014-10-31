package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.widget.Button;

import edu.chalmers.sikkr.R;
import edu.chalmers.sikkr.backend.messages.ListableMessage;
import edu.chalmers.sikkr.backend.messages.PlaybackListener;

/**
 * @author Oskar Jönefors
 *
 * Class to handle the animations of the message play buttons.
 */
public class PlayButtonHandler implements PlaybackListener {

    private final Button button;
    private final ListableMessage msg;
    private final Activity activity;

    public PlayButtonHandler(Button button, ListableMessage msg, Activity activity) {
        this.button = button;
        this.msg = msg;
        this.activity = activity;
    }


    @Override
    public void playbackStarted() {

        final int animNbr = msg.isRead() ? R.drawable.old_message_play : R.drawable.new_message_play;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setBackgroundResource(animNbr);

                AnimationDrawable anim = (AnimationDrawable) button.getBackground();
                anim.start();
            }});
        }

        @Override
    public void playbackDone() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setBackgroundResource(R.drawable.old_message_1);
            }});
    }

    @Override
    public void playbackError() {
        playbackDone();
    }
}
