package com.armdroid.rxfilechooser.content;

import android.net.Uri;

public class AudioContent extends FileContent {

    private long mDuration;

    public AudioContent(String path, Uri uri, long size, long duration) {
        super(path, uri, size);
        mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }
}
