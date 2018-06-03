package com.armdroid.rxfilechooser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.armdroid.rxfilechooser.chooser.RxFileChooser
import com.armdroid.rxfilechooser.content.AudioContent
import com.armdroid.rxfilechooser.content.ImageContent
import com.armdroid.rxfilechooser.content.VideoContent

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        mSpinner = findViewById(R.id.spinner)
        button.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        val switch = findViewById<Switch>(R.id.switchView)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val textView = findViewById<TextView>(R.id.textView)

        val rxFileChooser = RxFileChooser.from(this)
        val observable = when (mSpinner.selectedItemPosition) {
            0 -> rxFileChooser.getImage(switch.isChecked)
            1 -> rxFileChooser.file
            2 -> rxFileChooser.getVideo(switch.isChecked)
            3 -> rxFileChooser.getImageOrVideo(switch.isChecked)
            4 -> rxFileChooser.audio
            5 -> rxFileChooser.takePhoto()
            6 -> rxFileChooser.takeVideo()
            7 -> rxFileChooser.recordAudio()
            8 -> rxFileChooser.openChooserForImage(switch.isChecked)
            9 -> rxFileChooser.openChooserForVideo(switch.isChecked)
            else -> rxFileChooser.getImage(switch.isChecked)
        }
        observable.subscribe({
            val duration = when (it) {
                is ImageContent -> {
                    imageView.setImageBitmap(it.image)
                    0
                }
                is VideoContent -> {
                    imageView.setImageBitmap(it.thumbnail)
                    it.duration
                }
                is AudioContent -> it.duration
                else -> 0
            }

            val text = "Name: ${it.fileName} \n" +
                    "Size: ${it.size} \n" +
                    "Path: ${it.path} \n" +
                    "Uri: ${it.uri} \n" +
                    "Duration: $duration"
            textView.text = text

        }) {
            it.printStackTrace()
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }
    }
}
