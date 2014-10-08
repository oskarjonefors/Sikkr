package edu.chalmers.sikkr.backend.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * A simple class to write messages to a log file on the Android device.
 * @author Oskar Jönefors
 */
public class LogUtility {

    public static final String TAG = "WriteLogUtility";

    /**
     * Write the given log rows to a log file with the given name. The file name will be stripped of
     * all non-alphanumerical characters, and the log will be placed in the 'sikkr' directory in
     * the android data directory.
     *
     * @param fileName
     * @param logRows
     */
    public static void writeLogFile(String fileName, String... logRows) {
        final String fName = fixFilename(fileName);

        if(fName.length() > 0 && logRows.length > 0) {
            String logPath = getLogDirectory();
            File dir = new File(logPath);

            /* Create log directory if there isn't one */
            dir.mkdirs();

            File logFile = new File(logPath + fName + ".txt");

            if(!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                for(String logLine : logRows) {
                    buf.append(logLine);
                    buf.newLine();
                }
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * This will remove all illegal characters from the file name.
     * @param str
     * @return
     */
    private static String fixFilename(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Return the absolute path of the log directory.
     * @return
     */
    public static String getLogDirectory() {
        String logPath;
        if(Environment.getExternalStorageState().equals("mounted")){
            logPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            logPath = Environment.getDataDirectory().getAbsolutePath();
        }
        logPath += "/sikkr/logs/";
        Log.d(TAG, "Target log path is " + logPath);
        return logPath;
    }

    /**
     * Returns a timestamp of the current file in the format YYYY-MM-DD_HH-MM-SS
     * @return - A string timestamp
     */
    public static String getTimeStamp() {
        final Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" +
                cal.get(Calendar.DAY_OF_MONTH) + "_" + cal.get(Calendar.HOUR_OF_DAY) + "-" +
                cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND);
    }
}
