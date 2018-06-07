package com.armdroid.rxfilechooser.request_helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.FileContent;

import java.io.File;
import java.net.URLConnection;

import io.reactivex.Observable;

public abstract class RequestHelper {
    protected FileChooser mFileChooser;

    protected Uri mUri;
    protected String mFilePath;
    protected boolean mReturnBitmap = false;

    public RequestHelper(FileChooser fileChooser) {
        this.mFileChooser = fileChooser;
    }

    public abstract Intent getIntent();

    public abstract String[] getPermissions();

    public String getType() {
        return "";
    }

    protected void setupMediaFile(String type) {
        String fileName;
        if (type.equals(MediaStore.ACTION_VIDEO_CAPTURE)) {
            fileName = "VID_" + System.currentTimeMillis() + ".mp4";
        } else {
            fileName = "IMG_" + System.currentTimeMillis() + ".jpeg";
        }
        setUriAndPath(fileName);
    }

    public void setUriAndPath(String fileName) {
        Activity activity = mFileChooser.getActivity();
        File parentFile = new File(Environment.getExternalStorageDirectory(), getAppName(activity));
        parentFile.mkdir();
        File mediaFile = new File(parentFile, fileName);
        if (mediaFile.exists()) {
            mediaFile = getAlternativeFile(parentFile, fileName);
        }
        try {
            mUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", mediaFile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        mFilePath = mediaFile.getPath();
    }

    private File getAlternativeFile(File parentFile, String fileName) {
        int indexOfDot = fileName.lastIndexOf(".");
        String name = fileName.substring(0, indexOfDot);
        String ext = fileName.substring(indexOfDot + 1);

        int index = 1;
        File mediaFile;
        while (true) {
            String test = name + "_" + index + "." + ext;
            mediaFile = new File(parentFile, test);
            if (mediaFile.exists()) {
                index++;
                continue;
            }
            break;
        }
        return mediaFile;
    }

    private String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ((String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "FileChooser"))
                .replaceAll(" ", "");
    }

    public Intent getMultipleIntent() {
        Intent intent = getIntent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        return intent;
    }

    public String getMimeType(String path, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = mFileChooser.getActivity().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        if (mimeType == null) {
            mimeType = URLConnection.guessContentTypeFromName(path);
        }
        return mimeType;
    }

    protected Observable<FileContent> trySendToGallery(FileContent fileContent) {
        MediaScannerConnection.scanFile(
                mFileChooser.getActivity(), new String[] { fileContent.getPath() }, null,
                (path, uri) -> {

                });
        return Observable.just(fileContent);
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public boolean returnBitmap() {
        return mReturnBitmap;
    }
}
