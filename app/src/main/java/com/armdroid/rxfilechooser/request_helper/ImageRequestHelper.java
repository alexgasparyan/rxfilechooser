package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.ImageContent;

import java.util.List;

import io.reactivex.Observable;


public class ImageRequestHelper extends RequestHelper {

    private boolean mFromGallery = true;

    public ImageRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose image only from gallery application
     *
     * @return the same instance of {@link ImageRequestHelper}
     */
    public ImageRequestHelper fromGallery() {
        mFromGallery = true;
        return this;
    }

    /**
     * Choose image from file system (which includes gallery application)
     *
     * @return the same instance of {@link ImageRequestHelper}
     */
    public ImageRequestHelper fromFileSystem() {
        mFromGallery = false;
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance
     *
     * @return the same instance of {@link ImageRequestHelper}
     */
    public ImageRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Choose single image file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<ImageContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery)
                .flatMap(fileContent -> Observable.just(((ImageContent) fileContent)));
    }

    /**
     * Choose multiple image files
     *
     * @return an {@link Observable} containing list of files
     */
    public Observable<List<ImageContent>> multiple() {
        return mFileChooser.startMultipleAction(this)
                .flatMap(Observable::fromIterable)
                .flatMap(this::trySendToGallery)
                .map(file -> (ImageContent) file)
                .toList()
                .toObservable();
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(mFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    @Override
    public String getType() {
        return "image";
    }
}
