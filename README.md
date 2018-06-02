# FileChooser

Extremely simple, lightweight library for choosing files from android device. 

![image](https://raw.githubusercontent.com/alexgasparyan/rxfilechooser/master/sample.gif)  


**Choose:**
* Image, video or both by opening a chooser (from internal storage, cloud, gallery or camera)
* Audio files (from internal storage or audio recorder)
* Any kind of file from internal storage or cloud

**Advantages:**
* Universal code for all android versions and devices
* Easy to use
* Permission handling set in library using RxPermissions library (https://github.com/tbruyelle/RxPermissions)
* Rich info when file is chosen (name, size, path, uri, duration (for audio and video), bitmap (for image and video))
* `minSdkVersion 17` (Android 4.2 Jelly Bean)
* Support for both fragment and activity


## Usage ##

A sample project is attached to library where all the features of library are used. You can also test devices and OS versions with this.

Add dependency in app module gradle file:

```gradle
implementation 'com.armdroid:rxfilechooser:1.0.0'
```

To initialize the library add this code to `Application` class.
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(content -> imageView.setImageBitmap(content.getImage())), throwable ->{
                        throwable.printStackTrace();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    };

    }
}
```
It is recommended that all actions are done in background thread (in some cases files are copied which can take some time)

When the resources are not needed anymore (i.e. in `onDestroy`), clean them by calling:

```java
RxFileChooser.from(activity).release();
```


## Note ##
* Library does not guarantee that the chosen file has the type specified by user if chosen from third party cloud or internal storage apps (i.e. Google Drive, Dropbox, ES Explorer, Astro). Accordingly, library throws `WrongFileTypeException`.
* Library uses provider with authority name `{YOUR_PACKAGE_NAME}.fileProvider` (see merged manifest)

## Supported devices and OS
Please add device names with OS version that have successfully passed all tests available in sample project in the comments of this gist:

https://gist.github.com/alexgasparyan/7f130d9571e7c413a904b3ff031e37f0

## More ##
* Contribution and error reporting is very much appreciated
