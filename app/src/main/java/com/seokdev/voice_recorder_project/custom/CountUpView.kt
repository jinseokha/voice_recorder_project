package com.seokdev.voice_recorder_project.custom

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * @author Ha Jin Seok
 * @email seok270@gmail.com
 * @created 2022-01-09
 * @desc
 */

class CountUpView(
    context : Context,
    attributeSet : AttributeSet? = null
) : AppCompatTextView(context, attributeSet) {

    private var startTimeStamp : Long = 0L

    private val countUpAction : Runnable = object : Runnable {
        override fun run() {
            val currentTimeStamp = SystemClock.elapsedRealtime()

            val countTimeSeconds = ((currentTimeStamp - startTimeStamp)/1000L).toInt()
            updateCountTime(countTimeSeconds)
            handler?.postDelayed(this, 1000L)
        }
    }

    fun startCountUp() {
        startTimeStamp = SystemClock.elapsedRealtime()
        handler?.post(countUpAction)
    }

    fun stopCountUp() {
        handler?.removeCallbacks(countUpAction)
    }

    fun clearCountTime() {
        updateCountTime(0)
    }

    private fun updateCountTime(countTimeSeconds : Int) {
        val hours = countTimeSeconds / 60 / 60
        val minutes = countTimeSeconds / 60
        val seconds = countTimeSeconds % 60
        text = "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}