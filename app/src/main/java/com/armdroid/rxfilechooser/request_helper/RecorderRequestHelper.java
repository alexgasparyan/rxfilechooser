package com.armdroid.rxfilechooser.request_helper;

import android.Manifest;
import android.content.Intent;
import android.provider.MediaStore;

import com.armdroid.rxfilechooser.FileChooser;
import com.armdroid.rxfilechooser.content.AudioContent;

import io.reactivex.Observable;


public class RecorderRequestHelper extends RequestHelper{

    public RecorderRequestHelper(FileChooser mFileChooser) {
        super(mFileChooser);
    }

    /**
     * Record single audio
     *
     * @return an {@link Observable} containing the file instance
     */
    public Observable<AudioContent> single() {
        return mFileChooser.startAction(this)
                .flatMap(fileContent -> Observable.just(((AudioContent) fileContent)));
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        return intent;
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
    }

    @Override
    public String getType() {
        return "audio";
    }
}
