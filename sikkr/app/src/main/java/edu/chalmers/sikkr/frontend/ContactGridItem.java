package edu.chalmers.sikkr.frontend;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author Oskar Jönefors
 */
public class ContactGridItem {

    private Bitmap image;
    private String name;

    public ContactGridItem(Bitmap image, String name) {
        super();
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
    }

    public void setName(String name) {
        this.name = name;
    }
}
