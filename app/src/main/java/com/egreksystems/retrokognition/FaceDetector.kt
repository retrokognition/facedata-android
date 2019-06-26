package com.egreksystems.retrokognition

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Facing
import com.otaliastudios.cameraview.Frame
import java.lang.Exception

class FaceDetector {

    private lateinit var faceDetectionListener: IFaceDetectionListener

    private val options = FirebaseVisionFaceDetectorOptions.Builder()
        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        .setMinFaceSize(0.15f)
        .build()

    /**
     * This method, generates the Meta Data for a FirebaseVisionImage
     * Metadata, includes: imageWidth, imageHeight, imageFormat and imageRotation
     * @param imageRotation Rotation of the Image to be detected for Faces
     * @param imageWidth Width of given Image
     * @param imageHeight Width of given Image
     * @return FirebaseVisionImageMetadata containing information about Image
     */
    private fun generateFirebaseImageMetaData(
        imageRotation: Int,
        imageWidth: Int,
        imageHeight: Int
    ): FirebaseVisionImageMetadata {
        return with(FirebaseVisionImageMetadata.Builder()) {
            setWidth(imageWidth)
            setHeight(imageHeight)
            setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            setRotation(imageRotation)
            build()
        }
    }

    fun detectFaces(
        frame: Frame,
        cameraManager: CameraManager,
        context: Context,
        activity: Activity
    ) {

        val imageData = frame.data
        val frameSize = frame.size
        val cameraId = cameraManager.cameraIdList[Facing.FRONT.ordinal]
        val imageRotation = CameraUtils.getRotationCompensation(cameraId, activity, context)
        val metadata =
            generateFirebaseImageMetaData(imageRotation, frameSize.width, frameSize.height)

        val firebaseVisionImage = imageData?.let { FirebaseVisionImage.fromByteArray(imageData, metadata) }

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        detector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener(activity) { faces ->
                if (faces.isNotEmpty()) {
                    faceDetectionListener.onFaceDetectSuccess(FaceData(faces[0]))
                } else {
                    faceDetectionListener.onNoFaceDetected()
                }
            }
            .addOnFailureListener(activity) { exception ->
                faceDetectionListener.onFaceDetectFailure(exception.localizedMessage)
            }

        detector.close()

    }

    fun setFaceDetectionListener(listener: IFaceDetectionListener) {
        faceDetectionListener = listener
    }

}