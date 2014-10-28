package edu.chalmers.sikkr.backend.messages;

import android.net.Uri;

/**
 * Created by ivaldi on 2014-10-01.
 */
public interface VoiceMessage extends ListableMessage {

    /**
     * @return the absolute file path of the voice message.
     */
    Uri getFileUri();

}
