package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class OvalCoverView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val dimColor = ContextCompat.getColor(context, R.color.semi_transparent)

    private var rectF: RectF = RectF()

    init {
        paint.color = dimColor
        paint.style = Paint.Style.FILL
    }


    private fun drawClippedOval(canvas: Canvas, rectF: RectF){

        val height = height.toFloat()
        val width = width.toFloat()

        rectF.set(width * 0.18f, height * 0.18f, width * 0.82f, height * 0.68f)

        canvas.drawOval(rectF, paint)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClippedOval(canvas, rectF)
    }
}