package com.egreksystems.retrokognition

class LivenessDetectionEvents private constructor(val smileEvent: Int, val blinkEvent: Int, val eventQueue: LivenessEventQueue){

    data class Builder(private var smile: Int = NO_LIVENESS, private var blink: Int = NO_LIVENESS, private val eventQueue: LivenessEventQueue = LivenessEventQueue()) {

        fun setEvent(event: Int): Builder{
            when(event){
                SMILE -> { this.smile = SMILE; eventQueue.enqueue(SMILE)}
                BLINK -> { this.blink = BLINK; eventQueue.enqueue(BLINK)}
            }

            return this
        }

        fun build() = LivenessDetectionEvents(smile, blink, eventQueue)
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