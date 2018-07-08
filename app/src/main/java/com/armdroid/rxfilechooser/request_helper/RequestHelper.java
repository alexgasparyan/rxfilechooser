package com.armdroid.rxfilechooser.request_helper;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Pair;

import com.armdroid.rxfilechooser.content.FileContent;
import com.armdroid.rxfilechooser.utils.FileUtils;
import com.yalantis.ucrop.UCrop;

import io.reactivex.Observable;

public abstract class RequestHelper<CHILD extends RequestHelper<CHILD>> {

    protected FileChooser mFileChooser;
    protected Uri mUri;
    protected String mFilePath;
    protected boolean mReturnBitmap;
    protected boolean mInternalReturnBitmap;
    protected boolean mDoCrop;
    protected UCrop.Options mCropOptions;
    protected boolean mUseInternalStorage;

    protected RequestHelper(FileChooser fileChooser) {
        this.mFileChooser = fileChooser;
    }

    /**
     * Use filesDir/{YOUR_APP_NAME} as the location for all image saving operations.
     * This also removes need for storage permissions in some cases.
     *
     * @return the same instance of {@link CHILD}
     */
    public CHILD useInternalStorage() {
        mUseInternalStorage = true;
        return ((CHILD) this);
    }

    /**
     * Use externalDir/{YOUR_APP_NAME} as the location for all image saving operations
     *
     * @return the same instance of {@link CHILD}
     */
    public CHILD useExternalStorage() {
        mUseInternalStorage = false;
        return ((CHILD) this);
    }

    protected abstract Intent getIntent();

    protected abstract String[] getPermissions();

    protected String getType() {
        return "";
    }

    protected Intent getMultipleIntent() {
        Intent intent = getIntent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        return intent;
    }

    protected void setupMediaFile(String mediaType) {
        Pair<Uri, String> pair = FileUtils.getMediaFileFromType(mFileChooser.getActivity(), mediaType, mUseInternalStorage);
        mUri = pair.first;
        mFilePath = pair.second;
    }

    protected Observable<FileContent> trySendToGallery(FileContent fileContent) {
        MediaScannerConnection.scanFile(
                mFileChooser.getActivity(), new String[] { fileContent.getPath() }, null,
                (path, uri) -> {

                });
        return Observable.just(fileContent);
    }
}
