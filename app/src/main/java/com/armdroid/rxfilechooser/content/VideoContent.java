package com.armdroid.rxfilechooser.content;

import android.graphics.Bitmap;
import android.net.Uri;

public class VideoContent extends FileContent {

    private Bitmap mThumbnail;
    private long mDuration;

    public VideoContent(String path, Uri uri, long size, Bitmap thumbnail, long duration) {
        super(path, uri, size);
        mThumbnail = thumbnail;
        mDuration = duration;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }
}
