package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import edu.chalmers.sikkr.backend.VoiceMessage;

/**
 * Created by ivaldi on 2014-10-02.
 */
public class VoiceMessagePlayer {

    private final String TAG = "VoiceMessagePlayer";
    private final static VoiceMessagePlayer singleton = new VoiceMessagePlayer();
    private Context context;
    private MediaPlayer player;

    private VoiceMessagePlayer() {}

    private void setup(Context context) {
        this.context = context;
    }

    public static VoiceMessagePlayer getSharedInstance() {
        if(singleton.context == null) {
            throw new UnsupportedOperationException("Context must be supplied through the method" +
                    "setupSingleton(Context context) before an instance can be returned.");
        }
        return singleton;
    }

    public static void setupSingleton(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }
        singleton.setup(context);
    }

    public void playMessage(VoiceMessage msg) {
        if(player != null) {
            player.release();
            player = null;
        }
        player = new MediaPlayer();

        try {
            Log.d(TAG, "Trying to play message " + msg.getFileUri());
            player.setDataSource(context, msg.getFileUri());
            Log.d(TAG, )
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to prepare MediaPlayer.");
        }
    }

    public void stop() {
        if(player != null) {
            player.stop();
            Log.d(TAG, "Stopping playback.");
        }
    }
}
