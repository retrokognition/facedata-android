package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class OvalOverlayView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var borderColor: Int = Color.LTGRAY
    private var borderWidth: Float = 4.0f

    private val dimColor = ContextCompat.getColor(context, R.color.semi_transparent)
    private val resizeRatio: Float = 1.25f
    private val clipClearance: Float = 8f

    private var path: Path = Path()
    var rectF: RectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClippedOval(canvas, rectF)
    }

    private fun drawClippedOval(canvas: Canvas, rectF: RectF){
        canvas.save()

        val height = height.toFloat()
        val width = width.toFloat()

        rectF.set(clipClearance, clipClearance, width - clipClearance, height - clipClearance)

        path.addOval(rectF, Path.Direction.CW)

        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawColor(dimColor)
        canvas.restore()

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        paint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)



        canvas.drawOval(rectF, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(measuredWidth, measuredHeight)
        val h = size * resizeRatio
        setMeasuredDimension(size, h.toInt())
    }
}