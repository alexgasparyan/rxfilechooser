package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.ImageContent;

import io.reactivex.Observable;


public class CameraRequestHelper extends RequestHelper {

    public CameraRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance
     *
     * @return the same instance of {@link CameraRequestHelper}
     */
    public CameraRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Take a single photo from camera
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<ImageContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery)
                .flatMap(fileContent -> Observable.just(((ImageContent) fileContent)));
    }

    @Override
    public Intent getIntent() {
        Activity activity = mFileChooser.getActivity();
        setupMediaFile(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        return "image";
    }
}
