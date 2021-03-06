# FileChooser

Extremely simple, lightweight library for choosing files from android device. 

![image](https://raw.githubusercontent.com/alexgasparyan/rxfilechooser/master/sample.gif)  


**Choose:**
* Image, video or both by opening a chooser (from internal storage, cloud, gallery or camera)
* Audio files (from internal storage or audio recorder)
* Any kind of file from internal storage or cloud]
* List of all images in gallery (MediaStore.Images)

**Advantages:**
* Universal code for all android versions and devices
* Easy to use
* Permission handling set in library using RxPermissions library (https://github.com/tbruyelle/RxPermissions)
* Available crop for images with various options using UCrop library (https://github.com/Yalantis/uCrop)
* Rich info when file is chosen (name, size, path, uri, duration (for audio and video), bitmap (for image and video))
* `minSdkVersion 19` (Android 4.4 Jelly Bean)
* Support for both fragment and activity


## Usage ##

Add the following to app level gradle file. This is required since library uses RxActivityResult library (https://github.com/VictorAlbertos/RxActivityResult)
```gradle
repositories {
    maven { 
        url "https://jitpack.io" 
    }
```

Add dependency in app module gradle file: <br />
[![Download](https://api.bintray.com/packages/alexgasparyan1997/android/RxFileChooser/images/download.svg)](https://bintray.com/alexgasparyan1997/android/RxFileChooser/_latestVersion)
```gradle
implementation 'com.armdroid:rxfilechooser:x.y.z'
```

To initialize the library (required for `RxActivityResult`) add this code to `Application` class.
```java
public class App extends Application {

    @Override
    protected void onCreate() {
        super.onCreate();
        RxFileChooser.register(this);
    }
}
```

Add necessary permissions to `Manifest` file that you would normally add. For example, to use camera add:

```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

In your `Activity` / `Fragment` make this call:
```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ImageView imageView = findViewById(R.id.image_view);
        RxFileChooser.from(this)
                    .takePhoto()
                    .useExternalStorage()
                    .includeBitmap()
                    .crop()
                    .single()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(content -> {
                        imageView.setImageBitmap(content.getImage());
                    }, throwable -> {
                        throwable.printStackTrace();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });

    }
}
```
It is recommended that all actions are done in background thread (in some cases files are copied which can take some time)

Besides various concrete actions, you can also pick specific files by providing list of desired mime types:
```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ImageView imageView = findViewById(R.id.image_view);
        
        //Customize crop
        UCrop.Options options = new UCrop.Options();
        options.withAspectRatio(4, 3);
        options.setStatusBarColor(Color.BLACK);
        options.setActiveWidgetColor(Color.GRAY);
        
        RxFileChooser.from(this)
                    .pickFile()
                    .withMimeTypes("image/jpeg", "image/png")
                    .useInternalStorage()
                    .includeBitmap()
                    .crop(options)
                    .multiple()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(files -> {
                        imageView.setImageBitmap(((ImageContent) files.get(0)).getImage());
                    }, throwable -> {
                        throwable.printStackTrace();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });

    }
}
```

## Note ##
* Library does not guarantee that the chosen file has the type specified by user if chosen from third party cloud or internal storage apps (i.e. Google Drive, Dropbox, ES Explorer, Astro). Accordingly, library throws `WrongFileTypeException`.
* In case actions is stopped, library throws `FilePickCanceledException`.
* Library uses provider with authority name `{YOUR_PACKAGE_NAME}.fileProvider` (see merged manifest)

## Supported devices and OS
Please add device names with OS version that have successfully passed all features that library provides

https://gist.github.com/alexgasparyan/7f130d9571e7c413a904b3ff031e37f0

## More ##
* Contribution and error reporting is very much appreciated
