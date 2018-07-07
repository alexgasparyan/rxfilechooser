package com.armdroid.rxfilechooser.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.IOException;

public class ImageUtils {

    public static Bitmap getOrientedBitmap(Context context, final Uri uri, final String pImagePath) {
        return getOrientedBitmap(context, uri, pImagePath, null);
    }

    public static Bitmap getOrientedBitmap(Context context, final Uri uri, final String pImagePath, @Nullable Intent intent) {
        Bitmap bitmap = getRawBitmap(context, pImagePath, uri);
        if (bitmap == null) {
            return null;
        }
        int orientation = getOrientation(context, uri, pImagePath);
        if (orientation != -1) {
            final Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        if (bitmap == null && intent != null) {
            bitmap = getImageSecondOption(intent);
        }
        return bitmap;
    }

    private static Bitmap getRawBitmap(Context context, String pImagePath, Uri uri) {
        int MAX_DIMEN = 1024;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pImagePath, options);

        if (options.outWidth > MAX_DIMEN || options.outHeight > MAX_DIMEN) {
            float widthRatio = ((float) options.outWidth) / ((float) MAX_DIMEN);
            float heightRatio = ((float) options.outHeight) / ((float) MAX_DIMEN);
            float maxRatio = Math.max(widthRatio, heightRatio);

            options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pImagePath, options);
        if (bitmap != null) {
            return bitmap;
        }
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap getImageSecondOption(Intent data) {
        if (data == null) {
            return null;
        }
        Bundle extras = data.getExtras();
        Bitmap bitmap = null;
        if (extras != null) {
            bitmap = extras.getParcelable("data");
        }
        return bitmap;
    }

    private static int getOrientation(Context context, Uri imageUri, String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            if (rotation == ExifInterface.ORIENTATION_UNDEFINED) {
                return getRotationFromMediaStore(context, imageUri);
            }

            return exifToDegrees(rotation);
        } catch (IOException e) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    private static int getRotationFromMediaStore(Context context, Uri imageUri) {
        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
        if (cursor == null) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        cursor.moveToFirst();
        int orientationColumnIndex = cursor.getColumnIndex(columns[1]);
        if (orientationColumnIndex == -1) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        int orientation = cursor.getInt(orientationColumnIndex);
        cursor.close();
        return orientation;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else {
            return 0;
        }
    }

}
