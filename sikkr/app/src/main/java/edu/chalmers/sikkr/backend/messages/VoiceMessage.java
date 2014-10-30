package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

/**
 * @author Oskar Jönefors
 */
public interface VoiceMessage extends ListableMessage {

    /**
     * @return the absolute file path of the voice message.
     */
    Uri getFileUri();

}
