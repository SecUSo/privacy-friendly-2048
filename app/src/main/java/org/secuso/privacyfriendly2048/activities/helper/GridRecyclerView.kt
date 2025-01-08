package org.secuso.privacyfriendly2048.activities.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class GridRecyclerView(context: Context, attr: AttributeSet?, defStyleAttr: Int): RecyclerView(context, attr, defStyleAttr) {

    companion object {
        val MOVE_DURATION = 150L
        val CHANGE_DURATION = 100L
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