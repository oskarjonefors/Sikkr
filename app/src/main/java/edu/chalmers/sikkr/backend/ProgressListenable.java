package edu.chalmers.sikkr.backend;

import edu.chalmers.sikkr.backend.util.ProgressListener;

/**
 * An interface for classes that can specify what they are working on, and how far in the progress
 * they've come.
 */
public interface ProgressListenable {

    void addProgressListener(ProgressListener listener);

    void removeProgressListener(ProgressListener listener);

    void notifyListeners(double progress, String taskMsg);

}
