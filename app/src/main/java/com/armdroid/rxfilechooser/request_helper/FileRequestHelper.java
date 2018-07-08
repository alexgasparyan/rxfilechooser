package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.armdroid.rxfilechooser.content.FileContent;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;


public class FileRequestHelper extends RequestHelper<FileRequestHelper> {

    @Nullable
    protected List<String> mMimeTypes;

    public FileRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Set list of mime types. Files with only these types can be chosen.
     * For example: image/jpeg, image/png
     *
     * @return the same instance of {@link FileRequestHelper}
     */
    public FileRequestHelper withMimeTypes(String... mimeTypes) {
        mMimeTypes = Arrays.asList(mimeTypes);
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance in case image is available
     * If the return file instance is an image, then bitmap is the actual image.
     * If it is a video, then bitmap is a thumbnail from video.
     *
     * @return the same instance of {@link FileRequestHelper}
     */
    public FileRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Crop image after choosing it (available for image).
     *
     * @return the same instance of {@link FileRequestHelper}
     */
    public FileRequestHelper crop() {
        mDoCrop = true;
        return this;
    }

    /**
     * Crop image after choosing it with custom options such as UI of the crop page, crop aspect
     * ratio, max or min sie of cropped image etc (available for image).
     *
     * @param cropOptions The options that are going to be used for crop session
     * @return the same instance of {@link FileRequestHelper}
     */
    public FileRequestHelper crop(UCrop.Options cropOptions) {
        mDoCrop = true;
        mCropOptions = cropOptions;
        return this;
    }

    /**
     * Choose single file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<FileContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery);
    }

    /**
     * Choose multiple files
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
    protected Intent getIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (mMimeTypes != null) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mMimeTypes.toArray());
        }
        return intent;
    }

    @Override
    protected String[] getPermissions() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }
}
