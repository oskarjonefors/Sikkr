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

public class SpeechRecognitionHelper{

    public static void run( Activity callingActivity){
        if( isSpeechRecognitionActivityPresented(callingActivity)){
            startRecognitionActivity(callingActivity);
        }else{
            Toast.makeText(callingActivity, "We will have to install Google Speech Search in order to use voice recognition", Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(callingActivity);
        }
    }

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



    private static void startRecognitionActivity(Activity callingActivity) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Select an application");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

        callingActivity.startActivityForResult(intent, SystemData.VOICE_RECOGNITION_REQUEST_CODE);
    }

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