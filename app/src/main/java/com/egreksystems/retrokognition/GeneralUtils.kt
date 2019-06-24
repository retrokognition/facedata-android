package com.egreksystems.retrokognition

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class GeneralUtils {

    companion object{

        val FACE_IN_OVAL_HAPTIC = 0x09f6d6

        val BUTTON_CLICK_HAPTIC = 0xf456de

        fun performHapticFeedback(context: Context, type: Int){

            when(type){
                FACE_IN_OVAL_HAPTIC -> vibrate(context, 30, 70)
                BUTTON_CLICK_HAPTIC -> vibrate(context, 25, 50)
            }


        }

        private fun vibrate(context: Context, duration: Long, amplitude: Int){
            val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                vibrator.vibrate(duration)
            }
        }
    }
}