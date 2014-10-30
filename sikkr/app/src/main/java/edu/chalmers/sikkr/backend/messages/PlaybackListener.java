package edu.chalmers.sikkr.backend.messages;

/**
 * @author Oskar Jönefors
 *
 * Listener that listens for message playback to finish.
 */
public interface PlaybackListener {

    void playbackStarted();

    void playbackDone();

    void playbackError();

}
