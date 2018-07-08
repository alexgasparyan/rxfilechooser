package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.content.VideoContent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;


public class VideoChooserRequestHelper extends RequestHelper<VideoChooserRequestHelper> {

    private boolean mFromGallery = true;

    public VideoChooserRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose video only from gallery application
     *
     * @return the same instance of {@link VideoChooserRequestHelper}
     */
    public VideoChooserRequestHelper fromGallery() {
        mFromGallery = true;
        return this;
    }

    /**
     * Choose video from file system (which includes gallery application)
     *
     * @return the same instance of {@link VideoChooserRequestHelper}
     */
    public VideoChooserRequestHelper fromFileSystem() {
        mFromGallery = false;
        return this;
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance. Included bitmap is a thumbnail from video
     *
     * @return the same instance of {@link VideoChooserRequestHelper}
     */
    public VideoChooserRequestHelper includeBitmap() {
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
                .flatMap(this::trySendToGallery)
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
    protected Intent getIntent() {
        Activity activity = mFileChooser.getActivity();

        PackageManager pm = activity.getPackageManager();
        Intent galleryIntent = new Intent(mFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("video/*");

        String mediaType = MediaStore.ACTION_VIDEO_CAPTURE;
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
        return "video";
    }
}
