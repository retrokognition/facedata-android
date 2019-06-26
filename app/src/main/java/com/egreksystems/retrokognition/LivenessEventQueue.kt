package com.egreksystems.retrokognition

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
        items.add(event)
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