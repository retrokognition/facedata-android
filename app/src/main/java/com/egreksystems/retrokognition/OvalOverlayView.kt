package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class OvalOverlayView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var borderColor: Int = Color.WHITE
    private var borderWidth: Float = 6.0f

    private val dimColor = ContextCompat.getColor(context, R.color.semi_transparent)

    private var path: Path = Path()
    private var rectF: RectF = RectF()


    private var ovalLeft: Float = 0f
    private var ovalTop: Float = 0f
    private var ovalRight: Float = 0f
    private var ovalBottom: Float = 0f

    init {
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        paint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClippedOval(canvas, rectF)

    }

    private fun drawClippedOval(canvas: Canvas, rectF: RectF){
        canvas.save()

        val height = height.toFloat()
        val width = width.toFloat()

        rectF.set(width * 0.18f, height * 0.18f, width * 0.82f, height * 0.68f)

        path.addOval(rectF, Path.Direction.CW)

        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawColor(dimColor)
        canvas.restore()

        canvas.drawOval(rectF, paint)

        ovalLeft = rectF.left
        ovalTop = rectF.top
        ovalRight = rectF.right
        ovalBottom = rectF.bottom

    }


    fun setPaintStyle(color: Int, isDashed: Boolean){
        this.paint.color = color
        this.paint.style = Paint.Style.STROKE
        this.paint.strokeWidth = borderWidth

        if (isDashed){
            this.paint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)

        } else {
            this.paint.pathEffect = null
        }

        invalidate()

    }

    fun getOvalLeft(): Float{
        return ovalLeft
    }

    fun getOvalTop(): Float{
        return ovalTop
    }

    fun getOvalRight(): Float{
        return ovalRight
    }

    fun getOvalBottom(): Float{
        return ovalBottom
    }

    fun getOvalX(): Float = rectF.centerX()

}