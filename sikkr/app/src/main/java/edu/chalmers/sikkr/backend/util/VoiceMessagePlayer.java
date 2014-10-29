package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.drm.DrmStore;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.chalmers.sikkr.backend.messages.PlaybackListener;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;

/**
 * @author Oskar Jönefors
 */
public class VoiceMessagePlayer implements MediaPlayer.OnCompletionListener {

    private final String TAG = "VoiceMessagePlayer";
    private final static VoiceMessagePlayer singleton = new VoiceMessagePlayer();
    private Context context;
    private MediaPlayer player;
    private PlaybackListener listener;

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

    public void playMessage(VoiceMessage msg, PlaybackListener listener) {
        try {
            if(player != null) {
                player.release();
            }

            if (this.listener != null) {
                this.listener.playbackDone();
            }

            this.listener = listener;

            final File parent = context.getFilesDir();
            final File tmp = new File(parent, "mms.tmp");

            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("Could not create parent directory");
                }
            }

            if (tmp.exists()) {
                if (!tmp.delete()) {
                    throw new IOException("Could not create tempfile");
                }
            }
            tmp.createNewFile();

            DataInputStream dis = new DataInputStream(context.getContentResolver().openInputStream(msg.getFileUri()));
            byte[] read = new byte[dis.available()];
            dis.readFully(read);
            dis.close();

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmp));
            dos.write(read);
            dos.flush();
            dos.close();

            tmp.deleteOnExit();
            player = new MediaPlayer();
            player.setOnCompletionListener(this);
            Log.d(TAG, "Trying to play message " +msg);
            player.setDataSource(context, Uri.fromFile(tmp));
            player.prepare();
            player.start();
            listener.playbackStarted();
        }  catch (Exception e) {
            LogUtility.writeLogFile("VoiceMessagePlayerLog", e);
            Log.e(TAG, "Failed to prepare MediaPlayer.");
        }
    }

    public void stop() {
        if(player != null) {
            player.stop();
            Log.d(TAG, "Stopping playback.");
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (listener != null) {
            listener.playbackDone();
        }
    }
}
