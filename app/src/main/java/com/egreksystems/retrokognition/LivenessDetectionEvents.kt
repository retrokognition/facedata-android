package com.egreksystems.retrokognition

class LivenessDetectionEvents private constructor(val smileEvent: Int, val blinkEvent: Int, val turnHeadRightEvent: Int, val turnHeadLeftEvent: Int, val eventQueue: LivenessEventQueue){

    data class Builder(private var smile: Int = NO_LIVENESS, private var blink: Int = NO_LIVENESS, private var turnHeadRight: Int = NO_LIVENESS, private var turnHeadLeft: Int = NO_LIVENESS, private val eventQueue: LivenessEventQueue = LivenessEventQueue()) {

        fun setEvent(event: Int): Builder{
            when(event){
                SMILE -> { this.smile = SMILE; eventQueue.enqueue(SMILE)}
                BLINK -> { this.blink = BLINK; eventQueue.enqueue(BLINK)}
                TURN_HEAD_RIGHT -> { this.turnHeadRight = TURN_HEAD_RIGHT; eventQueue.enqueue(TURN_HEAD_RIGHT)}
                TURN_HEAD_LEFT -> { this.turnHeadLeft = TURN_HEAD_LEFT; eventQueue.enqueue(TURN_HEAD_LEFT)}
            }

            return this
        }

        fun build() = LivenessDetectionEvents(smile, blink, turnHeadRight, turnHeadLeft, eventQueue)
    }




    companion object{
        @JvmStatic
        val BLINK: Int = 0x02F2

        @JvmStatic
        val SMILE: Int = 0x03F1

        @JvmStatic
        val TURN_HEAD_LEFT: Int = 0x04F0

        @JvmStatic
        val TURN_HEAD_RIGHT: Int = 0x03EF

        @JvmStatic
        val NO_LIVENESS = 0x01F0

    }
}