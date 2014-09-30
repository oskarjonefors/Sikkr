package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by Eric on 2014-09-25.
 */
public final class TextToSpeechUtility {

    private static TextToSpeech tts;
    private final static SetupListener setupListener = new SetupListener();

    private TextToSpeechUtility() {
        throw new UnsupportedOperationException("Cannot create instance");
    }

    public static void setupTextToSpeech(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), setupListener);

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
            } else {
                tts.setLanguage(Locale.UK);
                tts.speak("Welcome to Sicker", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

}
