package edu.chalmers.sikkr.backend.util;

/**
 * An interface for classes that can listen to other classes' work progress.
 */
public interface ProgressListener {

    /**
     *
     * @param progress - How far into the current task the ProgressListenable has come.
     *                 Double value ranges from 0 to 1;
     *
     * @param senderTag - A tag from the sender, for instance the name of the class.
     *
     * @param taskMsg - What the task is.
     */
    public void notifyProgress(double progress, String senderTag, String taskMsg);

}
