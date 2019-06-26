package com.egreksystems.retrokognition

interface ILivenessEventListener {

    fun onEventDetectionSuccess(event: Int)

    fun onEventDetectionCancelled()
}