package com.egreksystems.retrokognition

import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.util.*

class LivenessProcessor(events: LivenessDetectionEvents) {

    private lateinit var listener: ILivenessEventListener

    private val livenessEvents = events

    private val eventList = livenessEvents.eventQueue.getEventList().toMutableList()

    private var eyesOpenCount = 0

    private var eyesCloseCount = 0

    fun setLivenessEventListener(eventListener: ILivenessEventListener){
        this.listener = eventListener
    }

    fun detectEvent(faceData: FaceData, event: Int?){

        val face: FirebaseVisionFace = faceData.face

            when(event){
                LivenessDetectionEvents.SMILE -> {
                    Log.e("EVENT_SMILE", "-----Checking For Smile-----")
                    if (smileDetected(face)){
                        Log.e("EVENT_SMILE", "-----Smile Detected-----")
                        listener.onEventDetectionSuccess(LivenessDetectionEvents.SMILE)
                    }
                }

                LivenessDetectionEvents.BLINK -> {
                    Log.e("EVENT_BLINK", "-----Checking For Blink-----")
                    if (blinkDetected(face)){
                        Log.e("EVENT_BLINK", "-----Blink Detected-----")
                        listener.onEventDetectionSuccess(LivenessDetectionEvents.BLINK)
                    }
                }
            }


    }

    private fun smileDetected(face: FirebaseVisionFace): Boolean{
        val smileProbability = if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY){
            face.smilingProbability
        } else {
            0.0f
        }

        Log.e("SMILE_PROBABILITY", smileProbability.toString())

        return smileProbability > 0.85
    }

    private fun blinkDetected(face: FirebaseVisionFace): Boolean{
        val rightEyeOpenProbability = if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY){
            face.rightEyeOpenProbability
        } else {
            0.0f
        }

        val leftEyeOpenProbability = if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY){
            face.leftEyeOpenProbability
        } else {
            0.0f
        }

        Log.e("EYES_PROBABILITY", "---Right Eye: " + rightEyeOpenProbability.toString() + " ---Left Eye: " + leftEyeOpenProbability)

        if (rightEyeOpenProbability > 0.85 && leftEyeOpenProbability > 0.85){
            ++eyesOpenCount
        }

        if (rightEyeOpenProbability < 0.40 && leftEyeOpenProbability < 0.40){
            ++eyesCloseCount
        }

        return (eyesCloseCount >= 1 && eyesOpenCount >= 2)

    }

    fun resetEvents(){
        livenessEvents.eventQueue.clear()
        livenessEvents.eventQueue.getEventList().addAll(eventList)
        eyesCloseCount = 0
        eyesOpenCount = 0
    }

}