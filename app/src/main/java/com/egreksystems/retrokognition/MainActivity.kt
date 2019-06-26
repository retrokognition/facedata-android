package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.databinding.DataBindingUtil
import com.egreksystems.retrokognition.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.FrameProcessor


class MainActivity : IFaceDetectionListener, ILivenessEventListener, AppCompatActivity() {

    lateinit var camera: CameraView
    private lateinit var binding: ActivityMainBinding
    private lateinit var faceDetector: FaceDetector
    private lateinit var recordButton: RecordButtonView
    private var isFaceInOval: Boolean = false
    private lateinit var frameProcessor: FrameProcessor
    private var skipUnprocessedFrame = false
    private lateinit var events: LivenessDetectionEvents
    private lateinit var livenessProcessor: LivenessProcessor
    private var performLiveness = false
    private var isLivenessDone = false
    private var currentEvent: Int? = -1

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

        events = LivenessDetectionEvents.Builder().setEvent(LivenessDetectionEvents.BLINK)
            .setEvent(LivenessDetectionEvents.SMILE)
            .build()

        livenessProcessor = LivenessProcessor(events)

        livenessProcessor.setLivenessEventListener(this)

        frameProcessor = FrameProcessor { frame ->
            if (!skipUnprocessedFrame) {
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
            if (!recordButton.getPressedState()) {
                performLiveness = true
                currentEvent = events.eventQueue.dequeue()
                recordButton.enablePressed(true)
                GeneralUtils.performHapticFeedback(this, GeneralUtils.BUTTON_CLICK_HAPTIC)
                recordButton.isEnabled = false
            } else {
                performLiveness = false
            }
        }


    }

    private fun handleFaceDetected(faceData: FaceData, boundingBox: Rect) {

        if (boundingBox.left > binding.ovalOverlayView.getOvalLeft() &&
            boundingBox.top > binding.ovalOverlayView.getOvalTop() &&
            boundingBox.right < binding.ovalOverlayView.getOvalRight() &&
            boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()
        ) {
            if (!isFaceInOval) {
                binding.ovalOverlayView.setPaintStyle(ContextCompat.getColor(this, R.color.color_turquoise), false)
                recordButton.enableButton(true)
                isFaceInOval = true
                binding.instructionText.setText(R.string.press_record_button)
            }

            if (performLiveness && !isLivenessDone) {
                performLivenessEvent(faceData)
            }

        } else {
            if (isFaceInOval) {
                handleSpoof()
            }

        }
    }

    private fun handleSpoof() {
        binding.ovalOverlayView.setPaintStyle(Color.WHITE, true)
        recordButton.enableButton(false)
        recordButton.enablePressed(false)
        isFaceInOval = false

        isLivenessDone = false
        if (performLiveness) {
            livenessProcessor.resetEvents()
            performLiveness = false
        }

        binding.livenessInstructionBackground.visibility = View.GONE
        binding.livenessIndicator.visibility = View.GONE
        binding.livenessInstruction.visibility = View.GONE
        binding.instructionText.setText(R.string.position_face)
    }

    private fun performLivenessEvent(faceData: FaceData){
        livenessProcessor.detectEvent(faceData, currentEvent)
        binding.livenessInstructionBackground.visibility = View.VISIBLE
        when(currentEvent){
            LivenessDetectionEvents.SMILE -> {
                binding.livenessInstruction.setText(R.string.smile_brightly)
                binding.livenessIndicator.setImageResource(R.drawable.ic_smile)
            }
            LivenessDetectionEvents.BLINK -> {
                binding.livenessInstruction.setText(R.string.blink_your_eyes)
                binding.livenessIndicator.setImageResource(R.drawable.ic_blink)
            }
        }
        binding.livenessIndicator.visibility = View.VISIBLE
        binding.livenessInstruction.visibility = View.VISIBLE
    }

    override fun onFaceDetectSuccess(faceData: FaceData) {
        skipUnprocessedFrame = false
        val face: FirebaseVisionFace = faceData.face

        val boundingBox = face.boundingBox

        handleFaceDetected(faceData, boundingBox)

    }

    override fun onNoFaceDetected() {
        skipUnprocessedFrame = false
        handleSpoof()
    }

    override fun onFaceDetectFailure(errorMessage: String) {
        skipUnprocessedFrame = false
        handleSpoof()
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

    override fun onEventDetectionSuccess(event: Int) {
        if (event == currentEvent && events.eventQueue.isEmpty()) {
            isLivenessDone = true
        } else {
            currentEvent = events.eventQueue.dequeue()
        }
    }

    override fun onEventDetectionCancelled() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
