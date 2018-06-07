package com.armdroid.rxfilechooser;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.armdroid.rxfilechooser.request_helper.ImageVideoRequestHelper;
import com.armdroid.rxfilechooser.request_helper.RequestHelper;
import com.armdroid.rxfilechooser.content.AudioContent;
import com.armdroid.rxfilechooser.content.FileContent;
import com.armdroid.rxfilechooser.content.ImageContent;
import com.armdroid.rxfilechooser.content.VideoContent;
import com.armdroid.rxfilechooser.exception.FilePickCanceledException;
import com.armdroid.rxfilechooser.exception.MissingDataException;
import com.armdroid.rxfilechooser.exception.PermissionDeniedException;
import com.armdroid.rxfilechooser.exception.WrongFileTypeException;
import com.armdroid.rxfilechooser.utils.AudioVideoUtils;
import com.armdroid.rxfilechooser.utils.ImageUtils;
import com.armdroid.rxfilechooser.utils.UriUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

import static com.armdroid.rxfilechooser.exception.MissingDataException.TYPE_PATH;
import static com.armdroid.rxfilechooser.exception.MissingDataException.TYPE_URI;

public class FileChooser {

    private RxPermissions mRxPermissions;
    private WeakReference<Activity> mActivity;
    private WeakReference<Fragment> mFragment;
    private RequestHelper mRequestHelper;

    public void setActivity(Activity activity) {
        mActivity = new WeakReference<>(activity);
        mRxPermissions = new RxPermissions(activity);
    }

    public void setFragment(Fragment fragment) {
        mFragment = new WeakReference<>(fragment);
        mActivity = new WeakReference<>(fragment.getActivity());
        mRxPermissions = new RxPermissions(getActivity());
    }

    public Activity getActivity() {
        return mActivity.get();
    }


    public Observable<List<FileContent>> startMultipleAction(RequestHelper requestHelper) {
        mRequestHelper = requestHelper;
        return checkPermissions(requestHelper.getPermissions())
                .flatMap(perms -> setRxActivityResult(requestHelper.getMultipleIntent()))
                .flatMap(this::onActivityMultipleResult)
                .doOnComplete(this::cleanSession);
    }


    public Observable<FileContent> startAction(RequestHelper requestHelper) {
        mRequestHelper = requestHelper;
        return checkPermissions(requestHelper.getPermissions())
                .flatMap(perms -> setRxActivityResult(requestHelper.getIntent()))
                .flatMap(this::onActivityResult)
                .doOnComplete(this::cleanSession);

    }


    private Observable<Result> setRxActivityResult(Intent intent) {
        if (mFragment != null) {
            return RxActivityResult.on(mFragment.get()).startIntent(intent)
                    .flatMap(result -> Observable.just((Result) result));
        } else {
            return RxActivityResult.on(getActivity()).startIntent(intent)
                    .flatMap(result -> Observable.just((Result) result));
        }
    }


    private Observable<FileContent> onActivityResult(Result result) throws Exception {
        int resultCode = result.resultCode();
        Uri requestUri = mRequestHelper.getUri();
        Intent data = result.data();

        if (resultCode == Activity.RESULT_OK) {
            if (data == null && requestUri == null) {
                throw new FilePickCanceledException("Could not receive any file");
            } else {
                Uri uri = requestUri;
                if (data != null && data.getData() != null) {
                    uri = data.getData();
                }
                if (uri == null) {
                    throw new MissingDataException(TYPE_URI);
                }
                if (requestUri != null && !uri.equals(requestUri)) {
                    mRequestHelper.setFilePath(null);
                }
                return Observable.just(getContentFromUri(uri, data));
            }
        } else {
            throw new FilePickCanceledException("Could not receive any file");
        }
    }


    private Observable<List<FileContent>> onActivityMultipleResult(Result result) throws Exception {
        int resultCode = result.resultCode();
        Intent data = result.data();
        if (resultCode == Activity.RESULT_OK) {
            if (data == null || (data.getData() == null && (data.getClipData() == null || data.getClipData().getItemCount() == 0))) {
                throw new FilePickCanceledException("Could not receive any file");
            } else {
                List<FileContent> contents = new ArrayList<>();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    int selectedCount = clipData.getItemCount();
                    for (int i = 0; i < selectedCount; i++) {
                        contents.add(getContentFromUri(clipData.getItemAt(i).getUri(), data));
                        mRequestHelper.setUri(null);
                        mRequestHelper.setFilePath(null);
                    }
                } else {
                    contents.add(getContentFromUri(data.getData(), data));
                }
                return Observable.just(contents);
            }
        } else {
            return Observable.error(new FilePickCanceledException("Could not receive any file"));
        }

    }

    private FileContent getContentFromUri(Uri uri, Intent data) throws Exception {
        Pair<Uri, String> pair = setFilePath(uri);
        if (pair == null) {
            throw new MissingDataException(TYPE_PATH);
        }
        uri = pair.first;
        String path = pair.second;
        long fileSize = new File(path).length();
        String mimeType = getMimeType(path, uri);

        if (mimeType.startsWith("image")) {
            return getImageResponse(path, uri, fileSize, data);
        } else if (mimeType.startsWith("audio")) {
            return getAudioResponse(path, uri, fileSize);
        } else if (mimeType.startsWith("video")) {
            return getVideoResponse(path, uri, fileSize);
        } else {
            return getFileResponse(path, uri, fileSize);
        }
    }

    private String getMimeType(String path, Uri uri) throws WrongFileTypeException {
        String mimeType = mRequestHelper.getMimeType(path, uri);
        if (mimeType == null) {
            throw new WrongFileTypeException("Mime type of the file is missing");
        }

        if (!isDesiredMimeType(mimeType)) {
            throw new WrongFileTypeException("Mime type of the file does not match to the request mime type");
        }
        return mimeType;
    }

    private boolean isDesiredMimeType(String currentType) {
        if (mRequestHelper instanceof ImageVideoRequestHelper) {
            return currentType.startsWith("image") || currentType.startsWith("video");
        }
        return currentType.startsWith(mRequestHelper.getType());
    }

    private Observable<List<Permission>> checkPermissions(String[] permissions) {
        return mRxPermissions.requestEach(permissions)
                .flatMap(permission -> {
                    if (!permission.granted) {
                        return Observable.error(new PermissionDeniedException(permission));
                    }
                    return Observable.just(permission);
                })
                .toList()
                .toObservable();
    }

    private FileContent getImageResponse(String path, Uri uri, long fileSize, Intent data) {
        Bitmap bitmap = mRequestHelper.returnBitmap() ? ImageUtils.getOrientedBitmap(getActivity(), uri, path, data) : null;
        return new ImageContent(path, uri, fileSize, bitmap);
    }

    private FileContent getAudioResponse(String path, Uri uri, long fileSize) {
        return new AudioContent(path, uri, fileSize, AudioVideoUtils.getDuration(path));

    }

    private FileContent getVideoResponse(String path, Uri uri, long fileSize) {
        Bitmap thumb = mRequestHelper.returnBitmap() ? ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND) : null;
        return new VideoContent(path, uri, fileSize, thumb, AudioVideoUtils.getDuration(path));

    }

    private FileContent getFileResponse(String path, Uri uri, long fileSize) {
        return new FileContent(path, uri, fileSize);
    }

    private void cleanSession() {
        mRequestHelper = null;
        mActivity = null;
        mFragment = null;
        mRxPermissions = null;
    }

    private Pair<Uri, String> setFilePath(Uri uri) {
        String path = mRequestHelper.getFilePath() == null ? UriUtils.getRealPathFromUri(getActivity(), uri) : mRequestHelper.getFilePath();
        if (path == null) {
            File file = UriUtils.saveFileFromUri(getActivity(), uri, mRequestHelper);
            if (file == null) {
                return null;
            }
            path = mRequestHelper.getFilePath();
            uri = mRequestHelper.getUri();
        }
        return Pair.create(uri, path);
    }
}
