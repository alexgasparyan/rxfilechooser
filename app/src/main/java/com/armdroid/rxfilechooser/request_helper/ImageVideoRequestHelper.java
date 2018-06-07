package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.FileContent;

import java.util.List;

import io.reactivex.Observable;


public class ImageVideoRequestHelper extends RequestHelper {

    private boolean mFromGallery = true;

    public ImageVideoRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose image or video only from gallery application
     *
     * @return the same instance of {@link ImageVideoRequestHelper}
     */
    public ImageVideoRequestHelper fromGallery() {
        mFromGallery = true;
        return this;
    }

    /**
     * Choose image or video from file system (which includes gallery application)
     *
     * @return the same instance of {@link ImageVideoRequestHelper}
     */
    public ImageVideoRequestHelper fromFileSystem() {
        mFromGallery = false;
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance.
     * If the return file instance is an image, then bitmap is the actual image.
     * If it is a video, then bitmap is a thumbnail from video.
     *
     * @return the same instance of {@link ImageVideoRequestHelper}
     */
    public ImageVideoRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Choose single image or video file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<FileContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery);
    }

    /**
     * Choose multiple image or video files
     *
     * @return an {@link Observable} containing list of files
     */
    public Observable<List<FileContent>> multiple() {
        return mFileChooser.startMultipleAction(this)
                .flatMap(Observable::fromIterable)
                .flatMap(this::trySendToGallery)
                .toList()
                .toObservable();
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(mFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
    }

}
