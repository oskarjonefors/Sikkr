package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.os.Environment;

/**
 * Created by ivaldi on 2014-10-01.
 */
public class VoiceMessageRecorder {

    public enum RecordingState {
        RECORDING, STOPPED, RESET
    }

    private final static VoiceMessageRecorder singleton = new VoiceMessageRecorder();
    private Context context;
    private RecordingState state = RecordingState.RESET;
    private String targetPath;

    private VoiceMessageRecorder() {
        if(Environment.getExternalStorageState().equals("mounted")) {
            targetPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    private void setup(Context context) {
        this.context = context;
    }

    public VoiceMessageRecorder getSharedInstance() {
        if(context == null) {
            throw new UnsupportedOperationException("Context must be supplied through the method" +
                    "setupSingleton(Context context) before an instance can be returned.");
        } else {
            return singleton;
        }
    }

    public void setupSingleton(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        } else {
            this.context = context;
        }
    }

    public void startRecording() {

    }

    public void stopRecording() {

    }

    public RecordingState getRecordingState() {
        return state;
    }
}
