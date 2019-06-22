package com.egreksystems.retrokognition

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.databinding.DataBindingUtil
import com.egreksystems.retrokognition.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.otaliastudios.cameraview.CameraView

class MainActivity : IOnFaceDetected, AppCompatActivity() {

    lateinit var camera: CameraView
    private lateinit var binding: ActivityMainBinding
    private lateinit var faceDetector: FaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()

        faceDetector.setOnFaceDetected(this)

    }

    private fun init() {
        camera = binding.camera
        camera.setLifecycleOwner(this)
        faceDetector = FaceDetector()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        camera.addFrameProcessor { frame ->

            faceDetector.detectFaces(
                frame,
                cameraManager,
                Resources.getSystem().displayMetrics.widthPixels,
                Resources.getSystem().displayMetrics.heightPixels,
                this,
                this
            )

        }
    }

    override fun onFaceDetectSuccess(faceData: FaceData) {
        val face: FirebaseVisionFace = faceData.face

        val boundingBox = face.boundingBox

        if(boundingBox.left > binding.ovalOverlayView.getOvalLeft()&&
                boundingBox.top > binding.ovalOverlayView.getOvalTop()&&
                boundingBox.right < binding.ovalOverlayView.getOvalRight()&&
                boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()){
            binding.ovalOverlayView.setPaintStyle(ContextCompat.getColor(this, R.color.color_turquoise), false)
        } else {
            binding.ovalOverlayView.setPaintStyle(Color.WHITE, true)
        }

    }

    override fun onFaceDetectFailure(errorMessage: String) {
        Toast.makeText(this, getString(R.string.face_detection_error_message), Toast.LENGTH_LONG).show()
    }


}
