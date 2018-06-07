package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.FileContent;

import java.util.List;

import io.reactivex.Observable;


public class FileRequestHelper extends RequestHelper {

    public FileRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose single file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<FileContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(this::trySendToGallery);
    }

    /**
     * Choose multiple files
     *
     * @return an {@link Observable} containing list of files
     */
    public Observable<List<FileContent>> multiple() {
        return mFileChooser.startMultipleAction(this)
                .flatMap(Observable::fromIterable)
                .flatMap(this::trySendToGallery)
                .toList()
                .toObservable();
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }
}
