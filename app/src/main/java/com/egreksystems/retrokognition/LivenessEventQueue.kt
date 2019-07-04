package com.egreksystems.retrokognition

import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*

class LivenessEventQueue  {
    private val items: MutableList<Int> = mutableListOf()

    fun getEventList(): MutableList<Int> = items

    fun isEmpty(): Boolean = items.isEmpty()

    fun isNotEmpty(): Boolean = items.isNotEmpty()

    fun isNullOrEmpty(): Boolean = items.isNullOrEmpty()

    fun size(): Int = items.count()

    fun clear(): Unit = items.clear()

    fun enqueue(event: Int){
        if (!items.contains(event)){
            items.add(event)
        } else {
            throw IllegalStateException("You can't set the same liveness event more than ones")
        }


    }

    fun dequeue(): Int?{
        return if (this.isEmpty()){
            null
        } else {
            items.removeAt(0)
        }
    }


    fun peek(): Int? = items[0]

    override fun toString(): String = items.toString()


}