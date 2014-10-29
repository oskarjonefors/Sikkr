package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import edu.chalmers.sikkr.backend.messages.PlaybackListener;

/**
 * Created by Eric on 2014-09-25.
 */
public final class TextToSpeechUtility {

    private static TextToSpeech tts;
    private static Context context;
    private final static SetupListener setupListener = new SetupListener();
    private static PlaybackListener listener;

    private TextToSpeechUtility() {
        throw new UnsupportedOperationException("Cannot create instance");
    }

    public static void setupTextToSpeech(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), setupListener);
        TextToSpeechUtility.context = context;

    }

    public static boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    public static void readAloud(String msg) {
        readAloud(msg, null);
    }

    public static void readAloud(String msg, PlaybackListener playbackListener) {
        if (tts != null) {
            if (tts.isSpeaking() && listener != null) {
                listener.playbackDone();
            }
            final HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "" + UUID.randomUUID());
            tts.setOnUtteranceProgressListener(new SikkrUtteranceProgressListener(playbackListener));
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, map);
            listener = playbackListener;
        }
    }

    private static void removeTextToSpeech() {
        tts = null;
    }



    private final static class SetupListener implements TextToSpeech.OnInitListener {
        
        @Override
        public final void onInit(int status) {
            if (status == TextToSpeech.ERROR) {
                removeTextToSpeech();
                Toast.makeText(context, "Error setting up text to speech", Toast.LENGTH_LONG).show();
            } else {
                tts.setLanguage(Locale.ROOT);
            }
        }
    }

}
