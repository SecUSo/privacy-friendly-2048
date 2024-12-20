/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendly2048.activities.helper

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

/**
 * This class implements the Gestures Listener for swiping in the game
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */
open class Gestures(context: Context?) : OnTouchListener {
    private var gestureDetector: GestureDetector? = null
    private var cxt: Context? = null

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        cxt = context
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val res = gestureDetector!!.onTouchEvent(event)
        return res
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - (e1?.y ?: e2.y)
                val diffX = e2.x - (e1?.x ?: e2.x)
                if (abs(diffX.toDouble()) > abs(diffY.toDouble())) {
                    if (abs(diffX.toDouble()) > SWIPE_THRESHOLD && abs(velocityX.toDouble()) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            result = onSwipeRight()
                        } else {
                            result = onSwipeLeft()
                        }
                    } else {
                        result = nichts()
                    }
                } else {
                    if (abs(diffY.toDouble()) > SWIPE_THRESHOLD && abs(velocityY.toDouble()) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            result = onSwipeBottom()
                        } else {
                            result = onSwipeTop()
                        }
                    } else {
                        result = nichts()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    open fun onSwipeRight(): Boolean {
        return false
    }

    open fun onSwipeLeft(): Boolean {
        return false
    }

    fun nichts(): Boolean {
        return false
    }

    open fun onSwipeTop(): Boolean {
        return false
    }

    open fun onSwipeBottom(): Boolean {
        return false
    }

    companion object {
        private const val SWIPE_THRESHOLD = 50
        private const val SWIPE_VELOCITY_THRESHOLD = 0
    }
}


