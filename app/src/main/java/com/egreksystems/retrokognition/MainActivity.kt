package com.egreksystems.retrokognition

import android.content.Context
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
import com.otaliastudios.cameraview.FrameProcessor

class MainActivity : IFaceDetectionListener, AppCompatActivity() {

    lateinit var camera: CameraView
    private lateinit var binding: ActivityMainBinding
    private lateinit var faceDetector: FaceDetector
    private lateinit var recordButton: RecordButtonView
    private var isFaceInOval: Boolean = false
    private lateinit var frameProcessor: FrameProcessor
    private var skipUnprocessedFrame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()

    }

    private fun init() {

        camera = binding.camera
        camera.setLifecycleOwner(this)
        skipUnprocessedFrame = false
        faceDetector = FaceDetector()
        faceDetector.setFaceDetectionListener(this)
        recordButton = binding.recordButton
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        frameProcessor = FrameProcessor { frame ->
           if (!skipUnprocessedFrame){
               Log.i("NEW_FRAME", "--New Frame Passed--")
               faceDetector.detectFaces(
                   frame,
                   cameraManager,
                   this,
                   this
               )
               skipUnprocessedFrame = true
           }
        }

        camera.addFrameProcessor(frameProcessor)

        recordButton.setOnClickListener {
            if (!recordButton.getPressedState()){
                recordButton.enablePressed(true)
                GeneralUtils.performHapticFeedback(this, GeneralUtils.BUTTON_CLICK_HAPTIC)
            }
        }

    }

    override fun onFaceDetectSuccess(faceData: FaceData) {
        skipUnprocessedFrame = false
        val face: FirebaseVisionFace = faceData.face

        val boundingBox = face.boundingBox

        if(boundingBox.left > binding.ovalOverlayView.getOvalLeft()&&
                boundingBox.top > binding.ovalOverlayView.getOvalTop()&&
                boundingBox.right < binding.ovalOverlayView.getOvalRight()&&
                boundingBox.bottom < binding.ovalOverlayView.getOvalBottom())
        {
            if (!isFaceInOval){
                binding.ovalOverlayView.setPaintStyle(ContextCompat.getColor(this, R.color.color_turquoise), false)
                recordButton.enableButton(true)
                isFaceInOval = true
            }

        } else {
            if (isFaceInOval){
                binding.ovalOverlayView.setPaintStyle(Color.WHITE, true)
                recordButton.enableButton(false)
                recordButton.enablePressed(false)
                isFaceInOval = false
            }

        }

    }

    override fun onNoFaceDetected() {
        skipUnprocessedFrame = false
        binding.ovalOverlayView.setPaintStyle(Color.WHITE, true)
        recordButton.enableButton(false)
        recordButton.enablePressed(false)
    }

    override fun onFaceDetectFailure(errorMessage: String) {
        skipUnprocessedFrame = false
        binding.ovalOverlayView.setPaintStyle(Color.WHITE, true)
        recordButton.enableButton(false)
        recordButton.enablePressed(false)
        Toast.makeText(this, getString(R.string.face_detection_error_message), Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        camera.removeFrameProcessor(frameProcessor)
        camera.close()
    }

    override fun onResume() {
        super.onResume()
        init()
    }


}
