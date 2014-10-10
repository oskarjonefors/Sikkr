package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Eric on 2014-09-25.
 */
public final class TextToSpeechUtility {

    private static TextToSpeech tts;
    private static Context context;
    private final static SetupListener setupListener = new SetupListener();

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
        if (tts != null) {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
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
                //Toast.makeText(context, "Successfully set up text to speech", Toast.LENGTH_LONG).show();
                //readAloud("Successfully set up text to speech");
            }
        }
    }

}
