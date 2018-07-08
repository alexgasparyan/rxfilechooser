package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.content.ImageContent;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;


public class ImageChooserRequestHelper extends RequestHelper<ImageChooserRequestHelper> {

    private boolean mFromGallery = true;

    public ImageChooserRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose image only from gallery application
     *
     * @return the same instance of {@link ImageChooserRequestHelper}
     */
    public ImageChooserRequestHelper fromGallery() {
        mFromGallery = true;
        return this;
    }

    /**
     * Choose image from file system (which includes gallery application)
     *
     * @return the same instance of {@link ImageChooserRequestHelper}
     */
    public ImageChooserRequestHelper fromFileSystem() {
        mFromGallery = false;
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance of image
     *
     * @return the same instance of {@link ImageChooserRequestHelper}
     */
    public ImageChooserRequestHelper includeBitmap() {
        mReturnBitmap = true;
        return this;
    }

    /**
     * Crop image after choosing it.
     *
     * @return the same instance of {@link ImageChooserRequestHelper}
     */
    public ImageChooserRequestHelper crop() {
        mDoCrop = true;
        return this;
    }

    /**
     * Crop image after choosing it with custom options such as UI of the crop page, crop aspect
     * ratio, max or min sie of cropped image etc.
     *
     * @param cropOptions The options that are going to be used for crop session
     * @return the same instance of {@link ImageChooserRequestHelper}
     */
    public ImageChooserRequestHelper crop(UCrop.Options cropOptions) {
        mDoCrop = true;
        mCropOptions = cropOptions;
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
    protected Intent getIntent() {
        Activity activity = mFileChooser.getActivity();

        PackageManager pm = activity.getPackageManager();
        Intent galleryIntent = new Intent(mFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        String mediaType = MediaStore.ACTION_IMAGE_CAPTURE;
        setupMediaFile(mediaType);
        Intent cameraIntent = new Intent(mediaType);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

        List<ResolveInfo> resInfo = pm.queryIntentActivities(cameraIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String pName = ri.activityInfo.packageName;
            Intent customIntent = new Intent(mediaType);
            customIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            customIntent.setComponent(new ComponentName(pName, ri.activityInfo.name));
            customIntent.setPackage(pName);
            activity.grantUriPermission(pName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentList.add(new LabeledIntent(customIntent, pName, ri.loadLabel(pm), ri.icon));
        }

        Intent intent = Intent.createChooser(galleryIntent, "Choose from");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new LabeledIntent[intentList.size()]));
        return intent;
    }

    @Override
    protected String[] getPermissions() {
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
    }

    @Override
    protected String getType() {
        return "image";
    }
}
