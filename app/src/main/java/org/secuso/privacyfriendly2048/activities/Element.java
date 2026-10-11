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


package org.secuso.privacyfriendly2048.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * One tile on the game field.
 * <p>
 * The game logic works on {@link #number} (the value the tile has after the current move), while
 * the screen shows {@link #dNumber} until the tile's animation reaches the point where the new value
 * should appear. {@link #dPosX}/{@link #dPosY} is where the tile belongs; the view is placed there
 * with its X/Y translation, so moving a tile never triggers a layout pass.
 * <p>
 * It is a plain View that draws a rounded rectangle and a pre-laid-out label from {@link TileStyle}.
 * Changing the value only re-records two draw operations; sliding, popping and fading only change
 * RenderNode properties and redraw nothing.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class Element extends View {
    public int number = 0;
    public int dNumber = 0;
    public int posX = 0;
    public int posY = 0;
    public int dPosX = 0;
    public int dPosY = 0;
    public boolean activated;
    public boolean animateMoving = false;

    private final TileStyle style;
    private int shown = 0;

    // ---- animation state, managed by TileAnimations ----
    /** Tile this one is being absorbed by in the current move, or null. */
    Element victimOf;
    /** Value to show while being absorbed, if the tile was itself still merging when absorbed. */
    int victimShow;
    /** A spawn is scheduled but has not appeared yet: keep the tile hidden until then. */
    boolean spawnPending;
    /** The tile still has to jump to its slot (it is an emptied or absorbed tile). */
    boolean jumpPending;
    /** Called when this tile lands on a tile it merges with. */
    Runnable onMerge;

    ValueAnimator moveAnimator;
    ValueAnimator jumpAnimator;
    ValueAnimator popAnimator;
    ValueAnimator spawnAnimator;
    boolean moveCancelled;
    boolean jumpCancelled;
    boolean moveMerge;
    float moveTargetX;
    float moveTargetY;
    final TileAnimations.Spring springX = new TileAnimations.Spring();
    final TileAnimations.Spring springY = new TileAnimations.Spring();

    public Element(Context c, TileStyle style) {
        super(c);
        this.style = style;
        setWillNotDraw(false);
        setVisibility(View.INVISIBLE);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /** Shows the logical value (and hides the tile when it is empty). */
    public void drawItem() {
        dNumber = number;
        activated = (number != 0);
        paint(number);
        int visibility = number == 0 ? View.INVISIBLE : View.VISIBLE;
        if (getVisibility() != visibility)
            setVisibility(visibility);
    }

    /** Changes what the tile displays without touching its logical value or visibility. */
    void paint(int value) {
        if (value == shown)
            return;
        shown = value;
        setContentDescription(value == 0 ? null : style.label(value));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        style.drawTile(canvas, shown, getWidth(), getHeight());
    }

    @Override
    public String toString() {
        return "number: " + number;
    }

    public int getNumber() {
        return number;
    }

    public void setDPosition(int i, int j) {
        dPosX = i;
        dPosY = j;
    }

    public void setNumber(int i) {
        number = i;
    }

    public int getdPosX() {
        return dPosX;
    }

    public int getdPosY() {
        return dPosY;
    }

    public int getdNumber() {
        return dNumber;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
