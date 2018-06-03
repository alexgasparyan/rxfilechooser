package com.armdroid.rxfilechooser

import android.app.Application
import com.armdroid.rxfilechooser.chooser.RxFileChooser

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        RxFileChooser.register(this)
    }
}