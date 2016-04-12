package com.fourteenelevendev.android.apps.ioexperiment.model;

/**
 * Basic letter object
 *
 * Created by kyleparker on 4/6/2016.
 */
public class Letter {
    private int imageResId;
    private int contentDescription;

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(int contentDescription) {
        this.contentDescription = contentDescription;
    }
}
