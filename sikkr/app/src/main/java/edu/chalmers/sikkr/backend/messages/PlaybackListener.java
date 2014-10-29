package edu.chalmers.sikkr.backend.messages;

/**
 * @author Oskar Jönefors
 *
 * Class to listen for message playback to finish.
 */
public interface PlaybackListener {

    void playbackStarted();

    void playbackDone();

}
