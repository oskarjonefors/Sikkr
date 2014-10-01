package edu.chalmers.sikkr.frontend;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by ivaldi on 2014-09-25.
 */
public class ContactGridItem {

    private Bitmap image;
    private String name;

    public ContactGridItem(Bitmap image, String name) {
        super();
        Log.d("ContactGridItem", "SETTING IMAGE " + image.toString());
        this.image = image;
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public void setImage(Bitmap image) {
        this.image = image;
        Log.d("ContactGridItem", "SETTING IMAGE " + image.toString());
    }

    public void setName(String name) {
        this.name = name;
    }
}
