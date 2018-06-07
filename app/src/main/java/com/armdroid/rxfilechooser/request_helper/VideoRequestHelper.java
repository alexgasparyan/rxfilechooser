package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.VideoContent;

import java.util.List;

import io.reactivex.Observable;


public class VideoRequestHelper extends RequestHelper {

    private boolean mFromGallery = true;

    public VideoRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose video only from gallery application
     *
     * @return the same instance of {@link VideoRequestHelper}
     */
    public VideoRequestHelper fromGallery() {
        mFromGallery = true;
        return this;
    }

    /**
     * Choose video from file system (which includes gallery application)
     *
     * @return the same instance of {@link VideoRequestHelper}
     */
    public VideoRequestHelper fromFileSystem() {
        mFromGallery = false;
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance. Included bitmap is a thumbnail from video
     *
     * @return the same instance of {@link VideoRequestHelper}
     */
    public VideoRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Choose single video file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<VideoContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(fileContent -> Observable.just(((VideoContent) fileContent)));
    }

    /**
     * Choose multiple video files
     *
     * @return an {@link Observable} containing list of files
     */
    public Observable<List<VideoContent>> multiple() {
        return mFileChooser.startMultipleAction(this)
                .flatMap(Observable::fromIterable)
                .flatMap(this::trySendToGallery)
                .map(file -> (VideoContent) file)
                .toList()
                .toObservable();
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(mFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    @Override
    public String getType() {
        return "video";
    }
}
