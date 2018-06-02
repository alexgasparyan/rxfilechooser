package com.armdroid.rxfilechooser.exception;

public class MissingDataException extends Exception {

    public static final int TYPE_URI = 1;
    public static final int TYPE_PATH = 2;

    private final int mType;

    public MissingDataException(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}
