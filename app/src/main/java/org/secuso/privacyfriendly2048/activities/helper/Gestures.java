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


package org.secuso.privacyfriendly2048.activities.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Swipe recognition for the game field.
 * <p>
 * A swipe is recognised as soon as the finger has travelled far enough, while it is still on the
 * screen, instead of waiting for the fling at the end of the gesture. Tiles start moving one
 * gesture-length earlier and slow swipes work too. The threshold is in dp, so it feels the same on
 * every screen density (it used to be 50 px). One gesture triggers at most one move.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */
public class Gestures implements View.OnTouchListener {

    private final float threshold;
    private float downX;
    private float downY;
    private boolean tracking = false;
    private boolean handled = false;
    private int pointerId = -1;

    public Gestures(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        threshold = Math.max(2f * slop, 18f * density);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pointerId = event.getPointerId(0);
                downX = event.getX();
                downY = event.getY();
                tracking = true;
                handled = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                check(event);
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(event.getActionIndex()) == pointerId) {
                    check(event);
                    tracking = false;
                }
                return true;
            case MotionEvent.ACTION_UP:
                check(event);
                if (tracking && !handled)
                    nichts();
                tracking = false;
                return true;
            case MotionEvent.ACTION_CANCEL:
                tracking = false;
                return true;
            default:
                return true;
        }
    }

    private void check(MotionEvent event) {
        if (!tracking || handled)
            return;
        int index = event.findPointerIndex(pointerId);
        if (index < 0)
            return;
        float dx = event.getX(index) - downX;
        float dy = event.getY(index) - downY;
        if (Math.max(Math.abs(dx), Math.abs(dy)) >= threshold) {
            handled = true;
            dispatch(dx, dy);
        }
    }

    private void dispatch(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) onSwipeRight();
            else onSwipeLeft();
        } else {
            if (dy > 0) onSwipeBottom();
            else onSwipeTop();
        }
    }

    public boolean onSwipeRight() {
        return false;
    }

    public boolean onSwipeLeft() {
        return false;
    }

    public boolean nichts() {
        return false;
    }

    public boolean onSwipeTop() {
        return false;
    }

    public boolean onSwipeBottom() {
        return false;
    }

}
