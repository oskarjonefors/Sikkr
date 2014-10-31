package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.chalmers.sikkr.R;

/**
 * A class to match clipart images to contacts.
 * @author Oskar Jönefors
 */
public class ClipartUtility {

    private final static String TAG = "ClipartUtility";
    private final static String FILE_NAME = "contact_clipart_connections.ser";
    private final Map<String,String> regMap;
    private final Context context;
    private final String mapFilePath;

    public ClipartUtility(Context context) {
        mapFilePath = getDataDirectory() + FILE_NAME;
        regMap = getRegMap();
        this.context = context;
    }

    private Map<String, String> getRegMap() {
        File file = new File(mapFilePath);
        if (file.exists()) {
            return readMapFile(file);
        } else {
            Log.d(TAG, "No map file found, returning new map.");
            return new HashMap<>();
        }
    }

    private Map<String, String> readMapFile(File file) {
        Log.d(TAG, "Trying to read map file " + file.getAbsolutePath());
        try {
            FileInputStream fileIn = new FileInputStream(file.getAbsolutePath());
            ObjectInput objIn = new ObjectInputStream(fileIn);
            Map<String,String> map = (Map<String,String>) objIn.readObject();
            objIn.close();
            fileIn.close();
            Log.d(TAG, "Map file " + file.getAbsolutePath() + " was successfully read.");
            return map;
        } catch (IOException e) {
            Log.e(TAG, "Map file " + file.getAbsolutePath() + " could not be read!");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Map file " + file.getAbsolutePath() +
                    " was read, but no map class could be found in it.");
            e.printStackTrace();
            return null;
        }
    }

    public void saveChanges() {
        try {

            File mapFile = new File(mapFilePath);
            if (!mapFile.exists()) {
                File dir = new File(getDataDirectory());
                dir.mkdirs();
                dir.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(mapFilePath);
            ObjectOutput objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(regMap);
            objOut.close();
            fileOut.close();
            Log.d(TAG, "Map file successfully saved to " + mapFilePath);
        } catch (IOException e) {
            Log.e(TAG, "Map file could not be saved to " + mapFilePath);
            e.printStackTrace();
        }
    }

    public Bitmap getContactImage(String id) {
        Resources res = context.getResources();

        if (regMap.containsKey(id)) {

            /* If contact previously has an image assigned, return it */

            int picID = res.getIdentifier(regMap.get(id), "drawable", context.getPackageName());
            return BitmapFactory.decodeResource(res, picID);

        } else {
            /* If not, find one that's not been taken */

            String[] pics = res.getStringArray(R.array.clipart_list);

            String newPic = null;

            for(String str : pics) {
                Log.d("CU", "ARRAY ELEMENT: " + str);
                if (!regMap.containsValue(str)) {
                    newPic = str;
                    break;
                }
            }

            if (newPic == null) {
                return null;
            } else {
                regMap.put(id, newPic);
                int picID = res.getIdentifier(regMap.get(id), "drawable", context.getPackageName());
                return BitmapFactory.decodeResource(res, picID);
            }
        }
    }

    private static String getDataDirectory() {
        String dataPath;
        if(Environment.getExternalStorageState().equals("mounted")){
            dataPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            dataPath = Environment.getDataDirectory().getAbsolutePath();
        }
        dataPath += "/sikkr/data/";
        Log.d(TAG, "Target data path is " + dataPath);
        return dataPath;
    }
}
