package com.armdroid.rxfilechooser.content;

import android.graphics.Bitmap;
import android.net.Uri;

public class ImageContent extends FileContent {

    private Bitmap mImage;

    public ImageContent(String path, Uri uri, long size, Bitmap image) {
        super(path, uri, size);
        mImage = image;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }
}
