package org.secuso.privacyfriendly2048.activities.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class GridRecyclerView(context: Context, attr: AttributeSet?, defStyleAttr: Int): RecyclerView(context, attr, defStyleAttr) {

    companion object {
        val MOVE_DURATION = 75L
        val MERGE_DURATION = 75L
        val CHANGE_DURATION = 100L // Changed from 1000L
        val MAX_DELAY = max(max(MOVE_DURATION, MERGE_DURATION), CHANGE_DURATION) + 25
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)

    init {
        itemAnimator?.changeDuration = CHANGE_DURATION
        itemAnimator?.moveDuration = MOVE_DURATION
    }

    lateinit var gestures: Gestures

    override fun onInterceptTouchEvent(e: MotionEvent?) = true
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return if (e != null) {
            gestures.onTouch(this, e)
        } else {
            super.onTouchEvent(e)
        }
    }
}