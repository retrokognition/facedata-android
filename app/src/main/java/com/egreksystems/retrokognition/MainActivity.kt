package com.egreksystems.retrokognition

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.otaliastudios.cameraview.CameraView

class MainActivity : AppCompatActivity() {

    lateinit var camera: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        camera = findViewById(R.id.camera)
        camera.setLifecycleOwner(this)
    }


}
