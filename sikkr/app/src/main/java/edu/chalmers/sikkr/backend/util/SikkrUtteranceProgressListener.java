package edu.chalmers.sikkr.backend.util;

import android.speech.tts.UtteranceProgressListener;

import edu.chalmers.sikkr.backend.messages.PlaybackListener;

/**
 * Created by ivaldi on 2014-10-29.
 */
public final class SikkrUtteranceProgressListener extends UtteranceProgressListener {

    private final PlaybackListener listener;

    public SikkrUtteranceProgressListener(PlaybackListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart(String s) {
        if (listener != null) {
            listener.playbackStarted();
        }
    }

    @Override
    public void onDone(String s) {
        if (listener != null) {
            listener.playbackDone();
        }
    }

    @Override
    public void onError(String s) {
        if (listener != null) {
            listener.playbackError();
        }
    }
}
