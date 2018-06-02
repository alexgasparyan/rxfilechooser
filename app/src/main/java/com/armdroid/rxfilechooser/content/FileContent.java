package com.armdroid.rxfilechooser.content;

import android.net.Uri;

import java.io.File;


public class FileContent {

    private String mPath;
    private Uri mUri;
    private String mFileName;
    private long mSize;

    public FileContent(String path, Uri uri, long size) {
        mPath = path;
        mUri = uri;
        mSize = size;
        mFileName = new File(path).getName();
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }
}
