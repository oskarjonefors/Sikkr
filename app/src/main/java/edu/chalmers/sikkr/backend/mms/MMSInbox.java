package edu.chalmers.sikkr.backend.mms;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import edu.chalmers.sikkr.backend.VoiceMessage;

/**
 * Created by Eric on 2014-10-02.
 */
public class MMSInbox {

    private Context context;
    private final List<VoiceMessage> voiceMessages = new ArrayList<VoiceMessage>();
    private final static MMSInbox singleton = new MMSInbox();

    public static void setContext(Context context) {
        singleton.context = context;
    }

    public static MMSInbox getSharedInstance() {
        if (singleton.context != null) {
            return singleton;
        } else {
            throw new UnsupportedOperationException("This singleton requires a context to be set.");
        }
    }

    public void refreshInbox() {
        voiceMessages.clear();
        loadInbox();
    }

    public void loadInbox() {

    }

    public List<VoiceMessage> getInboxContents() {
        return voiceMessages;
    }

}
