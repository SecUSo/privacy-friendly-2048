package org.secuso.privacyfriendly2048.activities.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendly2048.activities.adapter.Grid2048Adapter

class GridRecyclerView(context: Context, attr: AttributeSet?, defStyleAttr: Int): RecyclerView(context, attr, defStyleAttr) {

    companion object {
        val MOVE_DURATION = 150L
        val MERGE_DURATION = 100L
        val SPAWN_DURATION = 100L
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)

    init {
        itemAnimator = GridItemAnimator()
        itemAnimator?.changeDuration = MERGE_DURATION
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

    class GridItemAnimator: DefaultItemAnimator() {
        override fun animateChange(oldHolder: ViewHolder, newHolder: ViewHolder, preInfo: ItemHolderInfo, postInfo: ItemHolderInfo): Boolean {
            if (newHolder == oldHolder) {
                dispatchChangeFinished(oldHolder, true)
                return false
            }

            if (oldHolder is Grid2048Adapter.GridCellViewHolder && newHolder is Grid2048Adapter.GridCellViewHolder && oldHolder.btn.text.isEmpty()) {
                if (oldHolder.btn.text.isNotEmpty()) {
                    // The change is a merge event, we want the old view to become invisible
//                    oldHolder.itemView.alpha = 0.0f;
                    oldHolder.itemView.animate()
                        .alpha(0f) // Fade out the old view
                        .setDuration(MERGE_DURATION / 2)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                oldHolder.itemView.alpha = 1f // Reset alpha for future use
                                dispatchChangeFinished(oldHolder, true)
                            }
                        })
                        .start()
                    return true
                }

                oldHolder.itemView.animate()
                    .alpha(0f) // Fade out the old view
                    .setDuration(SPAWN_DURATION * 2)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            oldHolder.itemView.alpha = 1f // Reset alpha for future use
                            dispatchChangeFinished(oldHolder, true)
                        }
                    })
                    .start()

                // Animate the new holder's view
                newHolder.itemView.alpha = 0f // Start with the new view invisible
                newHolder.itemView.animate()
                    .alpha(1f) // Fade in the new view
                    .setDuration(SPAWN_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            dispatchChangeFinished(newHolder, false)
                        }
                    })
                    .start()
                return true
            }
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }

        override fun endAnimation(item: ViewHolder) {
            super.endAnimation(item)
            item.itemView.setAlpha(1f)
        }
    }
}