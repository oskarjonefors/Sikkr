package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.List;

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
            Toast.makeText(callingActivity, "We will have to install Google Speech Search in order to use voice recognition", Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(callingActivity);
        }
    }

    /**
     * A method that will ask to install google voice search if no speech recognition utility is found.
     * @param ownerActivity - the activity calling this method.
     */
    private static void installGoogleVoiceSearch(final Activity ownerActivity) {
        AlertDialog dialog  = new AlertDialog.Builder(ownerActivity).setMessage("It's necessary to install Google Voice Search").setTitle(
                "Install Google Voice Search from Google Play?")
                .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {

                        }
                    }
                })
                    .setNegativeButton("Cancel", null)
                    .create();

        dialog.show();
        }

    /**
     * A method to start the speech recognition.
     * @param callingActivity - the activity calling this method
     */
    private static void startRecognitionActivity(Activity callingActivity) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Select an application");
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