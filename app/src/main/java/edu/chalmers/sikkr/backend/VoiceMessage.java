package edu.chalmers.sikkr.backend;

import android.net.Uri;

import java.util.Calendar;

/**
 * Created by ivaldi on 2014-10-01.
 */
public interface VoiceMessage extends ListableMessage {

    /**
     * Return the absolute file path of the voice message.
     * @return
     */
    Uri getFileUri();

}
