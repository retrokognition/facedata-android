package com.egreksystems.retrokognition

interface IOnFaceDetected {

    fun onFaceDetectSuccess(faceData: FaceData)

    fun onFaceDetectFailure(errorMessage: String)

}