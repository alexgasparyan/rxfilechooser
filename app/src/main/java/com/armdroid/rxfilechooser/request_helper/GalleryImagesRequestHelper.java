package com.armdroid.rxfilechooser.request_helper;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.content.ImageContent;
import com.armdroid.rxfilechooser.utils.ImageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class GalleryImagesRequestHelper {

    private WeakReference<Activity> mActivity;
    private boolean mIncludeBitmap = false;

    public GalleryImagesRequestHelper(Activity activity) {
        this.mActivity = new WeakReference<>(activity);
    }

    /**
     * Include also {@link android.graphics.Bitmap} when returning file instance.
     * WARNING! There is a high risk of {@link OutOfMemoryError}, since {@link ImageUtils#getOrientedBitmap(Context, Uri, String)} returns large bitmap.
     * Prefer to avoid calling this method and instead load images yourself by either using image loader such as Glide
     * or call {@link ImageUtils#getOrientedBitmap(Context, Uri, String)} and modify the size of the bitmap.
     *
     * @return the same instance of {@link GalleryImagesRequestHelper}
     */
    public GalleryImagesRequestHelper includeBitmap() {
        mIncludeBitmap = true;
        return this;
    }

    /**
     * Request all image files available in gallery
     *
     * @return an {@link Observable} containing list of files
     */
    private Observable<List<ImageContent>> request() {
        List<ImageContent> imageContentList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.MediaColumns.DATA};

        Cursor cursor = mActivity.get().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        if (cursor == null) {
            return getCleanableObservable(imageContentList);
        }
        int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
        int pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            String imagePath = cursor.getString(pathColumnIndex);
            int id = cursor.getInt(idColumnIndex);
            imageContentList.add(getImageContent(id, imagePath));
        }
        cursor.close();
        return getCleanableObservable(imageContentList);
    }

    private ImageContent getImageContent(int id, String imagePath) {
        File imageFile = new File(imagePath);
        Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        Bitmap bitmap = mIncludeBitmap ? ImageUtils.getOrientedBitmap(mActivity.get(), imageUri, imagePath, null) : null;
        long fileSize = imageFile.length();
        return new ImageContent(imagePath, imageUri, fileSize, bitmap);
    }

    private <T> Observable<T> getCleanableObservable(T data) {
        return Observable.just(data).doOnComplete(() -> mActivity = null);
    }

}
