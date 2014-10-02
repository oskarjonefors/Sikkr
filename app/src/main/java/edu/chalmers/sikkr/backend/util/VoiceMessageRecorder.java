package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.os.Environment;

import edu.chalmers.sikkr.backend.VoiceMessage;

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
        } else {
            throw new UnsupportedOperationException("No external storage is present." +
                    " Cannot make audio recordings.");
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

    /**
     * If the recording is stopped and a voice message has been recorded, return this message.
     * @return - A VoiceMessage
     * @throws IllegalArgumentException
     *      If no recording has been done, or if the recording has not been stopped
     */
    public VoiceMessage getVoiceMessage() throws IllegalArgumentException {
        if(state == RecordingState.STOPPED) {

        } else {
            throw new IllegalArgumentException(state == RecordingState.RECORDING ? "Cannot get voice message," +
                    "recording has not been stopped." : "Cannot get voice message since one has not been recorded.")
        }
    }
}
