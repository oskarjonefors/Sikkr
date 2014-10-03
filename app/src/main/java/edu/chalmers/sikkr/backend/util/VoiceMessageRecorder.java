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

import edu.chalmers.sikkr.backend.VoiceMessage;
import edu.chalmers.sikkr.backend.mms.MMS;

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
    private String currentFilePath;
    private MediaRecorder recorder;

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

    private String generateFileName() {
        Calendar time = Calendar.getInstance();
        return time.get(Calendar.YEAR) + "-" + time.get(Calendar.MONTH) + "-" +
                time.get(Calendar.DAY_OF_MONTH) + "-" + UUID.randomUUID().getMostSignificantBits() +
                ".3gp";
    }

    public VoiceMessageRecorder getSharedInstance() {
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
            currentFilePath = targetPath + "/" + generateFileName();
            recorder.setOutputFile(currentFilePath);
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
        } else {
            throw new IllegalArgumentException("Cannot stop recording since no recording is running.");
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

            return new MMS(timeStamp, "0", Uri.fromFile(new File(currentFilePath)));
        } else {
            throw new IllegalArgumentException(state == RecordingState.RECORDING ? "Cannot get voice message," +
                    "recording has not been stopped." : "Cannot get voice message since one has not been recorded.");
        }
    }
}
