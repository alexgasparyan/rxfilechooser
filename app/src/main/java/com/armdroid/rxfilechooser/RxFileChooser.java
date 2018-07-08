package com.armdroid.rxfilechooser;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.armdroid.rxfilechooser.request_helper.AudioRequestHelper;
import com.armdroid.rxfilechooser.request_helper.CamcorderRequestHelper;
import com.armdroid.rxfilechooser.request_helper.CameraRequestHelper;
import com.armdroid.rxfilechooser.request_helper.FileChooser;
import com.armdroid.rxfilechooser.request_helper.FileRequestHelper;
import com.armdroid.rxfilechooser.request_helper.GalleryImagesRequestHelper;
import com.armdroid.rxfilechooser.request_helper.ImageChooserRequestHelper;
import com.armdroid.rxfilechooser.request_helper.ImageRequestHelper;
import com.armdroid.rxfilechooser.request_helper.ImageVideoRequestHelper;
import com.armdroid.rxfilechooser.request_helper.RecorderRequestHelper;
import com.armdroid.rxfilechooser.request_helper.VideoChooserRequestHelper;
import com.armdroid.rxfilechooser.request_helper.VideoRequestHelper;

import rx_activity_result2.RxActivityResult;


public class RxFileChooser {

    private static RxFileChooser mRxFileChooser;
    private FileChooser mFileChooser;

    private RxFileChooser() {
        this.mFileChooser = new FileChooser();
    }

    /**
     * Provides an instance of activity from where the request must be done
     *
     * @param activity current instance
     * @return an instance of RxFileChooser
     */
    public static RxFileChooser from(Activity activity) {
        if (mRxFileChooser == null) {
            mRxFileChooser = new RxFileChooser();
        }
        mRxFileChooser.mFileChooser.setActivity(activity);
        return mRxFileChooser;
    }

    /**
     * Provides an instance of fragment from where the request must be done
     *
     * @param fragment current instance
     * @return an instance of RxFileChooser
     */
    public static RxFileChooser from(Fragment fragment) {
        if (mRxFileChooser == null) {
            mRxFileChooser = new RxFileChooser();
        }
        mRxFileChooser.mFileChooser.setFragment(fragment);
        return mRxFileChooser;
    }

    /**
     * Required method so that {@link RxActivityResult} can be instantiated
     *
     * @param application current application instance
     */
    public static void register(Application application) {
        RxActivityResult.register(application);
    }

    /**
     * Complete action of picking image
     *
     * @return an instance of helper class
     */
    public ImageRequestHelper pickImage() {
        return new ImageRequestHelper(mFileChooser);
    }

    /**
     * Complete action of picking video
     *
     * @return an instance of helper class
     */
    public VideoRequestHelper pickVideo() {
        return new VideoRequestHelper(mFileChooser);
    }

    /**
     * Complete action of picking image or video
     *
     * @return an instance of helper class
     */
    public ImageVideoRequestHelper pickImageOrVideo() {
        return new ImageVideoRequestHelper(mFileChooser);
    }

    /**
     * Complete action of picking audio
     *
     * @return an instance of helper class
     */
    public AudioRequestHelper pickAudio() {
        return new AudioRequestHelper(mFileChooser);
    }

    /**
     * Complete action of picking file
     *
     * @return an instance of helper class
     */
    public FileRequestHelper pickFile() {
        return new FileRequestHelper(mFileChooser);
    }

    /**
     * Complete action of taking photo
     *
     * @return an instance of helper class
     */
    public CameraRequestHelper takePhoto() {
        return new CameraRequestHelper(mFileChooser);
    }

    /**
     * Complete action of taking video
     *
     * @return an instance of helper class
     */
    public CamcorderRequestHelper takeVideo() {
        return new CamcorderRequestHelper(mFileChooser);
    }

    /**
     * Complete action of recording audio
     *
     * @return an instance of helper class
     */
    public RecorderRequestHelper recordAudio() {
        return new RecorderRequestHelper(mFileChooser);
    }

    /**
     * Complete action of opening chooser in order to choose image
     *
     * @return an instance of helper class
     */
    public ImageChooserRequestHelper openChooserForImage() {
        return new ImageChooserRequestHelper(mFileChooser);
    }

    /**
     * Complete action of opening chooser in order to choose video
     *
     * @return an instance of helper class
     */
    public VideoChooserRequestHelper openChooserForVideo() {
        return new VideoChooserRequestHelper(mFileChooser);
    }

    /**
     * Complete action of getting images from gallery
     *
     * @return an instance of helper class
     */
    public GalleryImagesRequestHelper getGalleryImages() {
        return new GalleryImagesRequestHelper(mFileChooser.getActivity());
    }

}