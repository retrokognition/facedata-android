package com.egreksystems.retrokognition

interface IFaceDetectionListener {

    fun onFaceDetectSuccess(faceData: FaceData)

    fun onNoFaceDetected()

    fun onFaceDetectFailure(errorMessage: String)

}