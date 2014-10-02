package edu.chalmers.sikkr.backend;

import android.net.Uri;

import java.util.Calendar;

/**
 * Created by ivaldi on 2014-10-01.
 */
public interface VoiceMessage {

    /**
     * Return the Calendar timestamp for when the message was created.
     * @return
     */
    Calendar getTimestamp();

    /**
     * Return the phone number of the sender of the message.
     * If the message was created on the local device, this number will be "0"
     * If the sender uses a hidden number, the number will be "-1"
     *
     * @return A string representing a phone number, alternatively "0" or "-1" for two special cases.
     */
    String getSender();

    /**
     * Return the absolute file path of the voice message.
     * @return
     */
    Uri getFileUri();
}
