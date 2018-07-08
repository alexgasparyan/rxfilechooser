package com.armdroid.rxfilechooser.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.armdroid.rxfilechooser.content.FileContent;

import java.io.File;
import java.net.URLConnection;

import io.reactivex.annotations.NonNull;

public class FileUtils {

    @Nullable
    public static String getMimeType(Context context, String path, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
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

    @NonNull
    public static Pair<Uri, String> getMediaFileFromType(Context context, String type, boolean useInternalStorage) {
        String fileName;
        if (type.equals(MediaStore.ACTION_VIDEO_CAPTURE)) {
            fileName = "VID_" + System.currentTimeMillis() + ".mp4";
        } else {
            fileName = "IMG_" + System.currentTimeMillis() + ".jpeg";
        }
        return getMediaFileFromName(context, fileName, useInternalStorage);
    }

    @NonNull
    public static Pair<Uri, String> getMediaFileFromName(Context context, String fileName, boolean useInternalStorage) {
        File rootDirectory = useInternalStorage ? context.getFilesDir() : Environment.getExternalStorageDirectory();
        File parentFile = new File(rootDirectory, getAppName(context));
        parentFile.mkdir();
        File mediaFile = new File(parentFile, fileName);
        if (mediaFile.exists()) {
            mediaFile = getAlternativeFile(parentFile, fileName);
        }
        Uri uri = null;
        try {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", mediaFile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return Pair.create(uri, mediaFile.getPath());
    }

    public static void delete(FileContent content) {
        File file = new File(content.getPath());
        if (file.exists()) {
            file.delete();
        }
    }

    private static File getAlternativeFile(File parentFile, String fileName) {
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

    private static String getAppName(Context context) {
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
}
