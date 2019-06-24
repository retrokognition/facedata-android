package com.egreksystems.retrokognition

class LivenessDetectionOptions private constructor(val smile: Int, val blink: Int){

    data class Builder(private var smile: Int = NO_LIVENESS, private var blink: Int = NO_LIVENESS) {

        fun setOption(option: Int): Builder{
            when(option){
                SMILE -> this.smile = SMILE
                BLINK -> this.blink = BLINK
            }

            return this
        }

        fun build() = LivenessDetectionOptions(smile, blink)
    }

    fun getSmileOption(): Int{
        return smile
    }

    fun getBlinkOption(): Int{
        return blink
    }

    companion object{
        @JvmStatic
        val BLINK: Int = 0x02F2

        @JvmStatic
        val SMILE: Int = 0x03F1

        @JvmStatic
        val NO_LIVENESS = 0x01F0

    }
}