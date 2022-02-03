package com.seokdev.voice_recorder_project.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.seokdev.voice_recorder_project.R

/**
 * @author Ha Jin Seok
 * @email seok270@gmail.com
 * @created 2022-01-09
 * @desc
 */

class SoundVisualizerView(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {

    var onRequestCurrentAmplitude : (() -> Int) ?= null
    private val barsColor = Color.argb(200, 181, 111, 233)

    companion object {
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE = 25F

        // 오디오 레코더의 get max amplitude(진폭, 볼륨) 음성의 최대값의 short 타입 최대값임.
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat() // Float로 미리 타입 변환

        private const val ACTION_INTERVAL = 20L // 20밀리초
    }

    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = barsColor
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }

    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0

    private val visualizeRepeatAction : Runnable = object : Runnable {
        override fun run() {
            if (!isReplaying) {
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                replayingPosition++
            }

            invalidate()

            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    fun startVisualizing(isReplaying : Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing() {
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f
        var offsetX = drawingWidth.toFloat()

        drawingAmplitudes
            .let { amplitudes ->
                if (isReplaying)
                    amplitudes.takeLast(replayingPosition)
                else
                    amplitudes
            }
            .forEach { amplitude ->
                val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

                offsetX -= LINE_SPACE

                if (offsetX < 0)
                    return@forEach

                canvas.drawLine(
                    offsetX,
                    centerY - lineLength / 2F,
                    offsetX,
                    centerY + lineLength / 2F,
                    amplitudePaint
                )
            }
    }
}

