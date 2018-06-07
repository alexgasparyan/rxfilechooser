package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.VideoContent;

import io.reactivex.Observable;


public class CamcorderRequestHelper extends RequestHelper {

    public CamcorderRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance
     *
     * @return the same instance of {@link CamcorderRequestHelper}
     */
    public CamcorderRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Record single video
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<VideoContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery)
                .flatMap(fileContent -> Observable.just(((VideoContent) fileContent)));
    }

    @Override
    public Intent getIntent() {
        Activity activity = mFileChooser.getActivity();
        setupMediaFile(MediaStore.ACTION_VIDEO_CAPTURE);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        String packageName = intent.resolveActivity(activity.getPackageManager()).getPackageName();
        activity.grantUriPermission(packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
    }

    @Override
    public String getType() {
        return "video";
    }
}
