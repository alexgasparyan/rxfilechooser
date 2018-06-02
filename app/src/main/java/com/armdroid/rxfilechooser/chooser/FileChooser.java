package com.armdroid.rxfilechooser.chooser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

import static com.armdroid.rxfilechooser.exception.MissingDataException.TYPE_PATH;
import static com.armdroid.rxfilechooser.exception.MissingDataException.TYPE_URI;

/**
 * Created by Alex Gasparyan on 7/24/2017.
 */

public class FileChooser {

    protected static final int GET_IMAGE = 6233;
    protected static final int GET_VIDEO = 6234;
    protected static final int GET_FILE = 6235;
    protected static final int GET_AUDIO = 6236;
    protected static final int GET_IMAGE_VIDEO = 6237;
    protected static final int TAKE_PHOTO = 6238;
    protected static final int TAKE_VIDEO = 6239;
    protected static final int RECORD_AUDIO = 6240;
    protected static final int OPEN_CHOOSER_IMAGE = 6241;
    protected static final int OPEN_CHOOSER_VIDEO = 6242;

    private final String TYPE_VIDEO = "video";
    private final String TYPE_IMAGE = "image";
    private final String TYPE_AUDIO = "audio";
    private final String TYPE_IMAGE_OR_VIDEO = TYPE_IMAGE + " " + TYPE_VIDEO;
    private final String TYPE_FILE = "";

    private Uri mUri;
    private String mFilePath;
    private String mFileType;

    private RxPermissions mRxPermissions;
    private Activity mActivity;
    private Fragment mFragment;

    private ArrayList<String> mFilePaths;

    public FileChooser() {
        mFilePaths = new ArrayList<>();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
        mRxPermissions = new RxPermissions(activity);
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mRxPermissions = new RxPermissions(mActivity);
    }

    public void release() {
        for (String path : mFilePaths) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public Observable<FileContent> startAction(String[] permissions, int requestCode, boolean justFromGallery) {
        if (mFragment != null) {
            return checkPermissions(permissions)
                    .flatMap(perms -> RxActivityResult.on(mFragment).startIntent(getActionIntent(requestCode, justFromGallery)))
                    .flatMap(this::onActivityResult)
                    .doOnComplete(this::cleanSession);

        }
        return checkPermissions(permissions)
                .flatMap(perms -> RxActivityResult.on(mActivity).startIntent(getActionIntent(requestCode, justFromGallery)))
                .flatMap(this::onActivityResult)
                .doOnComplete(this::cleanSession);

    }


    private Observable<FileContent> onActivityResult(Result result) {
        int resultCode = result.resultCode();
        Intent data = result.data();

        if (resultCode == Activity.RESULT_OK) {
            if (data == null && mUri == null) {
                return Observable.error(new MissingDataException(TYPE_URI));
            } else {
                Uri uri = this.mUri;
                if (data != null && data.getData() != null) {
                    uri = data.getData();
                }
                if (uri == null) {
                    return Observable.error(new MissingDataException(TYPE_URI));
                }
                String path = getFilePath(uri);
                if (path == null) {
                    return Observable.error(new MissingDataException(TYPE_PATH));
                }
                long fileSize = new File(path).length();
                String mimeType = getMimeType(path, uri);
                if (mimeType == null) {
                    return Observable.error(new WrongFileTypeException("Mime mFileType of the file is missing"));
                }

                if (!isDesiredMimeType(mimeType)) {
                    return Observable.error(new WrongFileTypeException("Mime mFileType of the file does not match to the request"));
                }

                if (mimeType.startsWith(TYPE_IMAGE)) {
                    return replyToImageResponse(path, uri, fileSize, data);
                } else if (mimeType.startsWith(TYPE_AUDIO)) {
                    return replyToVideoResponse(path, uri, fileSize);
                } else if (mimeType.startsWith(TYPE_VIDEO)) {
                    return replyToVideoResponse(path, uri, fileSize);
                } else {
                    return replyToFileResponse(path, uri, fileSize);
                }
            }
        } else {
            return Observable.error(new FilePickCanceledException("Could not receive any file"));
        }
    }


    private Intent getActionIntent(int requestType, boolean justFromGallery) {
        Intent intent = null;
        switch (requestType) {
            case GET_IMAGE:
                mFileType = TYPE_IMAGE;
                intent = new Intent(justFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
                intent.setType(TYPE_IMAGE + "/*");
                break;
            case GET_IMAGE_VIDEO:
                mFileType = TYPE_IMAGE_OR_VIDEO;
                intent = new Intent(justFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
                intent.setType(TYPE_IMAGE + "/* " + TYPE_VIDEO + "/*");
                break;
            case GET_VIDEO:
                mFileType = TYPE_VIDEO;
                intent = new Intent(justFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
                intent.setType(TYPE_VIDEO + "/*");
                break;
            case GET_FILE:
                mFileType = TYPE_FILE;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TYPE_FILE + "*/*");
                break;
            case GET_AUDIO:
                mFileType = TYPE_AUDIO;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(TYPE_AUDIO + "/*");
                break;
            case TAKE_PHOTO:
                mFileType = TYPE_IMAGE;
                setupMediaFile(MediaStore.ACTION_IMAGE_CAPTURE);
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                String packageName = intent.resolveActivity(mActivity.getPackageManager()).getPackageName();
                mActivity.grantUriPermission(packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                break;
            case TAKE_VIDEO:
                mFileType = TYPE_VIDEO;
                setupMediaFile(MediaStore.ACTION_VIDEO_CAPTURE);
                intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                String packName = intent.resolveActivity(mActivity.getPackageManager()).getPackageName();
                mActivity.grantUriPermission(packName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                break;
            case RECORD_AUDIO:
                mFileType = TYPE_AUDIO;
                intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                break;
            case OPEN_CHOOSER_IMAGE:
            case OPEN_CHOOSER_VIDEO:
                mFileType = requestType == OPEN_CHOOSER_IMAGE ? TYPE_IMAGE : TYPE_VIDEO;
                PackageManager pm = mActivity.getPackageManager();
                Intent galleryIntent = new Intent(justFromGallery ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
                galleryIntent.setType(mFileType + "/*");

                String mediaType = mFileType.equals(TYPE_VIDEO) ? MediaStore.ACTION_VIDEO_CAPTURE : MediaStore.ACTION_IMAGE_CAPTURE;
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
                    mActivity.grantUriPermission(pName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intentList.add(new LabeledIntent(customIntent, pName, ri.loadLabel(pm), ri.icon));
                }

                intent = Intent.createChooser(galleryIntent, "Choose from");
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new LabeledIntent[intentList.size()]));
                break;
        }
        return intent;


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

    private Observable<FileContent> replyToImageResponse(String path, Uri uri, long fileSize, Intent data) {
        Bitmap bitmap = ImageUtils.getOrientedBitmap(mActivity, uri, path, data);
        return Observable.just(new ImageContent(path, uri, fileSize, bitmap));
    }

    private Observable<FileContent> replyToVideoResponse(String path, Uri uri, long fileSize) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        return Observable.just(new VideoContent(path, uri, fileSize, thumb, AudioVideoUtils.getDuration(path)));

    }

    private Observable<FileContent> replyToFileResponse(String path, Uri uri, long fileSize) {
        return Observable.just(new FileContent(path, uri, fileSize));
    }

    private void cleanSession() {
        this.mFilePath = null;
        this.mFileType = null;
        this.mUri = null;
        this.mActivity = null;
        this.mFragment = null;
        this.mRxPermissions = null;
    }

    private String getFilePath(Uri uri) {
        String path = mFilePath == null ? UriUtils.getRealPathFromUri(mActivity, uri) : mFilePath;
        this.mFilePath = null;
        if (path == null) {
            File file = UriUtils.saveFileFromUri(mActivity, uri);
            if (file == null) {
                return null;
            }
            path = file.getAbsolutePath();
            mFilePaths.add(path);
        }
        return path;
    }

    private void setupMediaFile(String type) {
        String fileName;
        if (type.equals(MediaStore.ACTION_VIDEO_CAPTURE)) {
            fileName = "VID_" + System.currentTimeMillis() + ".mp4";
        } else {
            fileName = "IMG_" + System.currentTimeMillis() + ".jpeg";
        }
        File parentFile = new File(Environment.getExternalStorageDirectory(), getAppName(mActivity));
        parentFile.mkdir();
        File mediaFile = new File(parentFile, fileName);
        try {
            mUri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".fileProvider", mediaFile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        mFilePath = mediaFile.getPath();
    }

    private String getMimeType(String path, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = mActivity.getContentResolver();
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

    private boolean isDesiredMimeType(String currentType) {
        if (mFileType.equals(TYPE_IMAGE_OR_VIDEO)) {
            return currentType.startsWith(TYPE_IMAGE) || currentType.startsWith(TYPE_VIDEO);
        } else {
            return currentType.startsWith(mFileType);
        }
    }
}
