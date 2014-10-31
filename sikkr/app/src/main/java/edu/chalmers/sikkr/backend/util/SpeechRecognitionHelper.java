package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;

import java.util.List;

import edu.chalmers.sikkr.R;

/**
 * A class to handle the speech recognition feature.
 * @author Jesper Olsson
 */

public class SpeechRecognitionHelper{

    /**
     * A method that a class that wants to use speech recognition can call upon.
     * The method will run startRecognitionActivity if such speech recognition utility is installed
     * @param callingActivity - the activity calling this method
     */
    public static void run( Activity callingActivity){
        if( isSpeechRecognitionActivityPresented(callingActivity)){
            startRecognitionActivity(callingActivity);
        }else{
            installGoogleVoiceSearch(callingActivity);
        }
    }

    /**
     * A method that will ask to install google voice search if no speech recognition utility is found.
     * @param ownerActivity - the activity calling this method.
     */
    private static void installGoogleVoiceSearch(final Activity ownerActivity) {
        AlertDialog dialog  = new AlertDialog.Builder(ownerActivity).setMessage(
                ownerActivity.getString(R.string.no_voice_search)).setTitle(
                ownerActivity.getString(R.string.install_voice_question))
                .setPositiveButton(ownerActivity.getString(R.string.install), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {

                        }
                    }
                })
                    .setNegativeButton(ownerActivity.getString(R.string.cancel), null)
                    .create();

        dialog.show();
        }

    /**
     * A method to start the speech recognition.
     * @param callingActivity - the activity calling this method
     */
    private static void startRecognitionActivity(Activity callingActivity) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, callingActivity.getString(R.string.say_command));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

        callingActivity.startActivityForResult(intent, SystemData.VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * A method to check wether a speech recognition utility is installed
     * @param callerActivity - the activity calling this method
     * @return true if speech recognition utility is installed, false otherwise.
     */
    private static boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            PackageManager pm = callerActivity.getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                return true;
            }
        }catch(Exception e){ }

        return false;
    }


}