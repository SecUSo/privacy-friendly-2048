/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 */

package org.secuso.privacyfriendly2048.activities;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * The board and its empty cells in a single drawable, instead of one view per cell.
 * It is drawn once and then only composited, so it costs nothing while tiles move over it.
 */
final class BoardBackground extends Drawable {

    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float boardRadius;
    private final int n;
    private final int gap;
    private final int cell;
    private final float cellRadius;

    BoardBackground(int boardColor, float boardRadius, int cellColor, int n, int gap, int cell, float cellRadius) {
        boardPaint.setColor(boardColor);
        cellPaint.setColor(cellColor);
        this.boardRadius = boardRadius;
        this.n = n;
        this.gap = gap;
        this.cell = cell;
        this.cellRadius = cellRadius;
    }

    @Override
    public void draw(Canvas canvas) {
        android.graphics.Rect b = getBounds();
        if (boardPaint.getAlpha() != 0)
            canvas.drawRoundRect(b.left, b.top, b.right, b.bottom, boardRadius, boardRadius, boardPaint);
        for (int i = 0; i < n; i++) {
            float top = b.top + gap + i * (cell + gap);
            for (int j = 0; j < n; j++) {
                float left = b.left + gap + j * (cell + gap);
                canvas.drawRoundRect(left, top, left + cell, top + cell, cellRadius, cellRadius, cellPaint);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // fixed appearance
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // fixed appearance
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
