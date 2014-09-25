package edu.chalmers.sikkr;

import android.graphics.Bitmap;

/**
 * Created by ivaldi on 2014-09-25.
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
