package com.egreksystems.retrokognition

import android.content.Context
import android.content.res.Resources
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

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

        camera.addFrameProcessor { frame ->
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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

        Log.e("FACE_BOX_LEFT", boundingBox.left.toString())
        Log.e("FACE_BOX_TOP", boundingBox.top.toString())
        Log.e("FACE_BOX_RIGHT", boundingBox.right.toString())
        Log.e("FACE_BOX_BOTTOM", boundingBox.bottom.toString())

        Log.e("OVAL_BOX_LEFT", binding.ovalOverlayView.getOvalLeft().toString())
        Log.e("OVAL_BOX_TOP", binding.ovalOverlayView.getOvalTop().toString())
        Log.e("OVAL_BOX_RIGHT", binding.ovalOverlayView.getOvalRight().toString())
        Log.e("OVAL_BOX_BOTTOM", binding.ovalOverlayView.getOvalBottom().toString())
        if(boundingBox.left > binding.ovalOverlayView.getOvalLeft()&&
                boundingBox.top > binding.ovalOverlayView.getOvalTop()&&
                boundingBox.right < binding.ovalOverlayView.getOvalRight()&&
                boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()){
            binding.ovalOverlayView.setPaintStyle(0xFF00FF00, false)
        }
        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY){
            Log.e("SIMLE PROB", face.smilingProbability.toString())
        }
    }

    override fun onFaceDetectFailure(errorMessage: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
