package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.media.MediaPlayer;

import edu.chalmers.sikkr.backend.messages.PlaybackListener;

/**
 * @author Oskar Jönefors
 *
 */
public class SoundClipPlayer {

    /**
     * Play the given resource file.
     *
     * @param resId
     */
    public static void playSound(Context c, int resId, final PlaybackListener listener) {
        final MediaPlayer player = MediaPlayer.create(c, resId);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                listener.playbackDone();
            }
        });

        player.start();
    }

}
