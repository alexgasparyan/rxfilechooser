package com.armdroid.rxfilechooser.utils;

import android.media.MediaMetadataRetriever;

public class AudioVideoUtils {

    public static long getDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (duration == null) {
            return 0;
        }
        try {
            return Long.parseLong(duration);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
