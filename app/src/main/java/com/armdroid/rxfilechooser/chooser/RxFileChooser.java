package com.armdroid.rxfilechooser.chooser;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.armdroid.rxfilechooser.content.AudioContent;
import com.armdroid.rxfilechooser.content.FileContent;
import com.armdroid.rxfilechooser.content.ImageContent;
import com.armdroid.rxfilechooser.content.VideoContent;

import io.reactivex.Observable;
import rx_activity_result2.RxActivityResult;

import static com.armdroid.rxfilechooser.chooser.FileChooser.GET_AUDIO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.GET_FILE;
import static com.armdroid.rxfilechooser.chooser.FileChooser.GET_IMAGE;
import static com.armdroid.rxfilechooser.chooser.FileChooser.GET_IMAGE_VIDEO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.GET_VIDEO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.OPEN_CHOOSER_IMAGE;
import static com.armdroid.rxfilechooser.chooser.FileChooser.OPEN_CHOOSER_VIDEO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.RECORD_AUDIO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.TAKE_PHOTO;
import static com.armdroid.rxfilechooser.chooser.FileChooser.TAKE_VIDEO;


public class RxFileChooser {

    private static RxFileChooser mRxFileChooser;
    private FileChooser mFileChooser;

    private RxFileChooser() {
        this.mFileChooser = new FileChooser();
    }

    public static RxFileChooser from(Activity activity) {
        if (mRxFileChooser == null) {
            mRxFileChooser = new RxFileChooser();
        }
        mRxFileChooser.mFileChooser.setActivity(activity);
        return mRxFileChooser;
    }

    public static RxFileChooser from(Fragment fragment) {
        if (mRxFileChooser == null) {
            mRxFileChooser = new RxFileChooser();
        }
        mRxFileChooser.mFileChooser.setFragment(fragment);
        return mRxFileChooser;
    }

    public static void register(Application application) {
        RxActivityResult.register(application);
    }

    public Observable<ImageContent> getImage(boolean justFromGallery) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        return mFileChooser.startAction(permissions, GET_IMAGE, justFromGallery)
                .flatMap(fileContent -> Observable.just(((ImageContent) fileContent)));
    }

    public Observable<VideoContent> getVideo(boolean justFromGallery) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        return mFileChooser.startAction(permissions, GET_VIDEO, justFromGallery)
                .flatMap(fileContent -> Observable.just(((VideoContent) fileContent)));
    }

    public Observable<FileContent> getImageOrVideo(boolean justFromGallery) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        return mFileChooser.startAction(permissions, GET_IMAGE_VIDEO, justFromGallery);
    }

    public Observable<AudioContent> getAudio() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        return mFileChooser.startAction(permissions, GET_AUDIO, false)
                .flatMap(fileContent -> Observable.just(((AudioContent) fileContent)));
    }

    public Observable<FileContent> getFile() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        return mFileChooser.startAction(permissions, GET_FILE, false);
    }

    public Observable<ImageContent> takePhoto() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        return mFileChooser.startAction(permissions, TAKE_PHOTO, false)
                .flatMap(fileContent -> Observable.just(((ImageContent) fileContent)));
    }

    public Observable<VideoContent> takeVideo() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        return mFileChooser.startAction(permissions, TAKE_VIDEO, false)
                .flatMap(fileContent -> Observable.just(((VideoContent) fileContent)));
    }

    public Observable<AudioContent> recordAudio() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        return mFileChooser.startAction(permissions, RECORD_AUDIO, false)
                .flatMap(fileContent -> Observable.just(((AudioContent) fileContent)));
    }

    public Observable<ImageContent> openChooserForImage(boolean justFromGallery) {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        return mFileChooser.startAction(permissions, OPEN_CHOOSER_IMAGE, justFromGallery)
                .flatMap(fileContent -> Observable.just(((ImageContent) fileContent)));
    }

    public Observable<VideoContent> openChooserForVideo(boolean justFromGallery) {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        return mFileChooser.startAction(permissions, OPEN_CHOOSER_VIDEO, justFromGallery)
                .flatMap(fileContent -> Observable.just(((VideoContent) fileContent)));
    }

    public void release() {
        mFileChooser.release();
    }
}
