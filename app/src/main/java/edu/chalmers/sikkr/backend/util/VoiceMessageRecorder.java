package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import edu.chalmers.sikkr.backend.messages.VoiceMessage;
import edu.chalmers.sikkr.backend.messages.MMS;

/**
 * A simple class for recording voice messages.
 * @author Oskar Jönefors
 */
public class VoiceMessageRecorder {

    public enum RecordingState {
        RECORDING, STOPPED, RESET
    }

    private final String TAG = "VoiceMessageRecorder";
    private final static VoiceMessageRecorder singleton = createNewInstance();
    private Context context;
    private RecordingState state = RecordingState.RESET;
    private String targetPath;
    private String currentFilePath;
    private MediaRecorder recorder;

    private static VoiceMessageRecorder createNewInstance() {
        try {
            return new VoiceMessageRecorder();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private VoiceMessageRecorder() throws IOException {
        if(Environment.getExternalStorageState().equals("mounted")){
            targetPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            targetPath = Environment.getDataDirectory().getAbsolutePath();
        }
        targetPath += "/sikkr/";
        File dir = new File(targetPath);
        Log.d(TAG, "Target path is " + targetPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not setup folder");
        }
    }

    private void setup(Context context) {
        this.context = context;
    }

    private String generateFileName() {
        Calendar time = Calendar.getInstance();
        return time.get(Calendar.YEAR) + "-" + time.get(Calendar.MONTH) + "-" +
                time.get(Calendar.DAY_OF_MONTH) + "-" + UUID.randomUUID().getMostSignificantBits() +
                ".3gp";
    }

    public static VoiceMessageRecorder getSharedInstance() {
        if(singleton.context == null) {
            throw new UnsupportedOperationException("Context must be supplied through the method" +
                    "setupSingleton(Context context) before an instance can be returned.");
        } else {
            return singleton;
        }
    }

    public static void setupSingleton(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }
        singleton.setup(context);
    }

    public void startRecording() {
        if(state == RecordingState.RESET) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            currentFilePath = targetPath + generateFileName();
            recorder.setOutputFile(currentFilePath);
            Log.d(TAG, "Output file set to " + currentFilePath);
            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e("VoiceMessageRecorder", "Failed to prepare recorder.");
            }
            recorder.start();
            state = RecordingState.RECORDING;
        }


    }

    /**
     * Stop recording. If recording is not running, an exception will be thrown.
     * @throws IllegalArgumentException - If no recording is currently running.
     */
    public void stopRecording() throws IllegalArgumentException {
        if(state == RecordingState.RECORDING) {
            recorder.stop();
            recorder.release();
            recorder = null;
            state = RecordingState.STOPPED;
            Log.d(TAG, "Stopped recording.");
        } else {
            throw new IllegalArgumentException("Cannot stop recording since no recording is running.");
        }
    }

    /**
     * Delete the current recording and reset the player.
     * This method can only be run when the recorder has stopped.
     */
    public void discardRecording() throws IOException {
        if (state == RecordingState.STOPPED) {
            File discardFile = new File(currentFilePath);
            if (!discardFile.delete()) {
                throw new IOException("Could not delete a temporary file");
            }
            state = RecordingState.RESET;
        } else {
            throw new IllegalArgumentException("Cannot discard recording since " +
                    (state == RecordingState.RECORDING ? "recording hasn't been stopped yet." :
            "there is no recorded file to discard."));
        }
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
            final Calendar c = Calendar.getInstance();
            final Calendar timeStamp = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.SECOND));

            Log.d(TAG, "MMS timestamp set to " + timeStamp);
            state = RecordingState.RESET;
            return new MMS(timeStamp, Uri.fromFile(new File(currentFilePath)), true);
        } else {
            throw new IllegalArgumentException(state == RecordingState.RECORDING ? "Cannot get voice message," +
                    "recording has not been stopped." : "Cannot get voice message since one has not been recorded.");
        }
    }
}
