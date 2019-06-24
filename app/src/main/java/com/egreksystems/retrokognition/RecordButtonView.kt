package com.egreksystems.retrokognition

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class RecordButtonView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var outCircleSize = 320
    private var inCircleSize: Float = 0f
    private var inCirclePercentage: Float = 0.75f
    private val outCircleBorderWidth = 12f
    private var isButtonEnabled = false
    private var isButtonPressed = false

    init {
        isEnabled = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isButtonEnabled){
            paint.color = Color.WHITE
            drawOutCircle(canvas)
            paint.color = ContextCompat.getColor(context, R.color.color_alizarin)
            drawInCircle(canvas)
        } else {
            paint.color = ContextCompat.getColor(context, R.color.color_btn_gray)
            drawOutCircle(canvas)
            paint.color = ContextCompat.getColor(context, R.color.color_btn_disable)
            drawInCircle(canvas)
        }


    }

    private fun drawOutCircle(canvas: Canvas){
        canvas.save()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = outCircleBorderWidth

        val radius = (outCircleSize / 2f) - outCircleBorderWidth
        canvas.drawCircle(outCircleSize/2f, outCircleSize/2f, radius, paint)

    }

    fun enableButton(enable: Boolean){
        isEnabled = enable
        isButtonEnabled = enable
        if(enable){
            GeneralUtils.performHapticFeedback(context, GeneralUtils.FACE_IN_OVAL_HAPTIC)
        }
        invalidate()
    }


    private fun drawInCircle(canvas: Canvas){

        paint.style = Paint.Style.FILL

        inCircleSize = outCircleSize * inCirclePercentage

        val radius = inCircleSize / 2f

        canvas.drawCircle((outCircleSize/2f), (outCircleSize/2f), radius, paint)

    }

    fun enablePressed(enable: Boolean){
        inCirclePercentage = if(enable){
            isButtonPressed = true
            0.68f
        } else {
            isButtonPressed = false
            0.75f
        }
       invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        outCircleSize = min(measuredWidth, measuredHeight)
        setMeasuredDimension(outCircleSize, outCircleSize)

    }

    fun getPressedState(): Boolean{
        return isButtonPressed
    }

}