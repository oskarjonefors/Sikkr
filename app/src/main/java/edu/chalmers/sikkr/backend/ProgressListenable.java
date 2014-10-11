package edu.chalmers.sikkr.backend;

import edu.chalmers.sikkr.backend.util.ProgressListener;

/**
 * An interface for classes that can specify what they are working on, and how far in the progress
 * they've come.
 */
public interface ProgressListenable {

    addProgressListener(ProgressListener listener);

    removeProgressListener(ProgressListener listener);

}
