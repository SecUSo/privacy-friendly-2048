package org.secuso.privacyfriendly2048.activities.helper

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendly2048.activities.adapter.Grid2048Adapter

class GridRecyclerView(context: Context, attr: AttributeSet?, defStyleAttr: Int): RecyclerView(context, attr, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)

    init {
//        itemAnimator = GridItemAnimator()
        itemAnimator?.changeDuration = 100
        itemAnimator?.moveDuration = 150
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

            Log.d("GridAnimator", "${oldHolder::class.java.name}, ${newHolder::class.java.name}")
            if (oldHolder is Grid2048Adapter.GridCellViewHolder && newHolder is Grid2048Adapter.GridCellViewHolder) {
                Log.d("GridAnimator", "${oldHolder.btn.text}, ${newHolder.btn.text}, ${oldHolder.btn.text == ""}, ${newHolder.btn.text != ""}")
                val delay = moveDuration;
                if (oldHolder.btn.text == "" && newHolder.btn.text != "") {
                    newHolder.itemView.setAlpha(0f)
                    newHolder.itemView.postDelayed({
                        newHolder.itemView.setAlpha(1f)
                        super.animateChange(oldHolder, newHolder, preInfo, postInfo)
                    }, 100)
                    return true
                } else {
                    return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
                }
            } else {
                return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
            }
        }

        override fun endAnimation(item: ViewHolder) {
            super.endAnimation(item)
            item.itemView.setAlpha(1f)
        }
    }
}