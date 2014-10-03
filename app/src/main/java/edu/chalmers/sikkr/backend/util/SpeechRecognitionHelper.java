package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.List;

public class SpeechRecognitionHelper{

    //TODO - installGoogleMethod shall be implemented

    public static void run( Activity callingActivity){
        if( isSpeechRecognitionActivityPresented(callingActivity)){
            startRecognitionActivity(callingActivity);
        }else{
            Toast.makeText(callingActivity, "We will have to install Google Speech Search in order to use voice recognition", Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(callingActivity);
        }
    }

    private static void installGoogleVoiceSearch(final Activity ownerActivity) {

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
        }catch(Exception e){

        }

        return false;
    }


}