package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.AudioContent;

import java.util.List;

import io.reactivex.Observable;


public class AudioRequestHelper extends RequestHelper {

    public AudioRequestHelper(FileChooser fileChooser) {
        super(fileChooser);
    }

    /**
     * Choose single audio file
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<AudioContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(fileContent -> Observable.just(((AudioContent) fileContent)));
    }

    /**
     * Choose multiple audio files
     *
     * @return an {@link Observable} containing list of files
     */
    public Observable<List<AudioContent>> multiple() {
        return mFileChooser.startMultipleAction(this)
                .flatMap(Observable::fromIterable)
                .map(file -> (AudioContent) file)
                .toList()
                .toObservable();
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    @Override
    public String getType() {
        return "audio";
    }
}
