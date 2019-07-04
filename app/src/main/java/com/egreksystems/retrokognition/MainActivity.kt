package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.postOnAnimation

import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.ToolbarBindingAdapter
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.egreksystems.retrokognition.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.FrameProcessor


class MainActivity : IFaceDetectionListener, ILivenessEventListener,  AppCompatActivity() {

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
    private var isTurnBack = false
    private var currentEvent: Int? = LivenessDetectionEvents.NO_LIVENESS
    private lateinit var livenessAnimatedDrawable: Drawable

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
            .setEvent(LivenessDetectionEvents.SMILE).setEvent(LivenessDetectionEvents.TURN_HEAD_RIGHT)
            .setEvent(LivenessDetectionEvents.TURN_HEAD_LEFT)
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
                currentEvent = events.eventQueue.dequeue() ?: LivenessDetectionEvents.NO_LIVENESS
                performLiveness = currentEvent != LivenessDetectionEvents.NO_LIVENESS
                recordButton.enablePressed(true)
                GeneralUtils.performHapticFeedback(this, GeneralUtils.BUTTON_CLICK_HAPTIC)
                recordButton.isEnabled = false
            } else {
                performLiveness = false
            }
        }

        livenessAnimatedDrawable = (binding.livenessIndicator.drawable)

//        binding.livenessIndicator.visibility = View.VISIBLE
//        binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_right))
//
//        binding.livenessIndicator.setOnClickListener {
//            (binding.livenessIndicator.drawable as AnimatedVectorDrawable).start()
//        }

    }

    private fun handleFaceDetected(faceData: FaceData) {

        if (checkIfFaceInPosition(faceData)) {
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

    private fun checkIfFaceInPosition(faceData: FaceData): Boolean {
        return when (currentEvent) {

            LivenessDetectionEvents.TURN_HEAD_RIGHT -> {
                (faceData.face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR))?.position?.x ?: 0f > binding.ovalOverlayView.getOvalLeft() &&
                        faceData.face.boundingBox.top > binding.ovalOverlayView.getOvalTop() &&
                        faceData.face.boundingBox.right < binding.ovalOverlayView.getOvalRight() &&
                        faceData.face.boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()
            }

            LivenessDetectionEvents.TURN_HEAD_LEFT -> {
                faceData.face.boundingBox.left > binding.ovalOverlayView.getOvalLeft() &&
                        faceData.face.boundingBox.top > binding.ovalOverlayView.getOvalTop() &&
                        (faceData.face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR))?.position?.x ?: 0f < binding.ovalOverlayView.getOvalRight() &&
                        faceData.face.boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()
            }

            else -> {
                faceData.face.boundingBox.left > binding.ovalOverlayView.getOvalLeft() &&
                        faceData.face.boundingBox.top > binding.ovalOverlayView.getOvalTop() &&
                        faceData.face.boundingBox.right < binding.ovalOverlayView.getOvalRight() &&
                        faceData.face.boundingBox.bottom < binding.ovalOverlayView.getOvalBottom()
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

    private fun performLivenessEvent(faceData: FaceData) {
        livenessProcessor.detectEvent(faceData, currentEvent)
        binding.livenessInstructionBackground.visibility = View.VISIBLE
        when (currentEvent) {
            LivenessDetectionEvents.SMILE -> {
                binding.livenessInstruction.setText(R.string.smile_brightly)
                //binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_smile_anim))
                startIconAnimation()
            }
            LivenessDetectionEvents.BLINK -> {
                binding.livenessInstruction.setText(R.string.blink_your_eyes)
                //binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_blink_anim))
                startIconAnimation()
            }
            LivenessDetectionEvents.TURN_HEAD_RIGHT -> {
                binding.livenessInstruction.setText(R.string.turn_head_right)
                //binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_right))
                startIconAnimation()
            }
            LivenessDetectionEvents.TURN_HEAD_LEFT -> {
                binding.livenessInstruction.setText(R.string.turn_head_left)
                //binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_left))
                startIconAnimation()
            }
        }
        binding.livenessIndicator.visibility = View.VISIBLE
        binding.livenessInstruction.visibility = View.VISIBLE
    }

    private fun startIconAnimation(){

        if (!(livenessAnimatedDrawable as Animatable).isRunning){
            when(currentEvent){
                LivenessDetectionEvents.SMILE -> binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_smile_anim))
                LivenessDetectionEvents.BLINK -> binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_blink_anim))
                LivenessDetectionEvents.TURN_HEAD_RIGHT -> {
                    isTurnBack = if (!isTurnBack){
                        binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_right))
                        true
                    } else {
                        binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_right_back))
                        false
                    }

                }
                LivenessDetectionEvents.TURN_HEAD_LEFT -> {
                    isTurnBack = if (!isTurnBack){
                        binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_left))
                        true
                    } else {
                        binding.livenessIndicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_turn_left_back))
                        false
                    }
                }

            }
            livenessAnimatedDrawable = (binding.livenessIndicator.drawable as AnimatedVectorDrawable)
            (livenessAnimatedDrawable as Animatable).start()
        }
    }

    override fun onFaceDetectSuccess(faceData: FaceData) {
        skipUnprocessedFrame = false

        handleFaceDetected(faceData)

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
            currentEvent = events.eventQueue.dequeue() ?: LivenessDetectionEvents.NO_LIVENESS
        }
        isTurnBack = false
    }

    override fun onEventDetectionCancelled() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
