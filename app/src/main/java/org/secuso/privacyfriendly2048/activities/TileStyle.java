/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 */

package org.secuso.privacyfriendly2048.activities;

import static org.secuso.privacyfriendly2048.helpers.ThemeResolverKt.GetColorInt;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.secuso.privacyfriendly2048.R;

/**
 * Everything the tiles of one board share, resolved once instead of on every redraw:
 * the colour palette, the tile geometry and, per value, the label text laid out at its final size.
 * <p>
 * The text size is chosen exactly like the uniform auto-size of the TextView the tiles used to be
 * (1..100 sp in 1 sp steps, the largest size whose single line fits inside the padded tile), so the
 * tiles look the same, but the search runs once per value and board instead of on every setText.
 */
final class TileStyle {

    private static final int MAX_EXP = 15; // 32768, larger values keep its colours (as before)

    private static final int[] FILL_ATTRS = {
            R.attr.buttonEmpty, R.attr.button2, R.attr.button4, R.attr.button8, R.attr.button16,
            R.attr.button32, R.attr.button64, R.attr.button128, R.attr.button256, R.attr.button512,
            R.attr.button1024, R.attr.button2048, R.attr.button4096, R.attr.button8192,
            R.attr.button16384, R.attr.button32768};
    private static final int[] TEXT_ATTRS = {
            R.attr.buttonEmptyText, R.attr.button2Text, R.attr.button4Text, R.attr.button8Text,
            R.attr.button16Text, R.attr.button32Text, R.attr.button64Text, R.attr.button128Text,
            R.attr.button256Text, R.attr.button512Text, R.attr.button1024Text,
            R.attr.button2048Text, R.attr.button4096Text, R.attr.button8192Text,
            R.attr.button16384Text, R.attr.button32768Text};

    final int size;
    final float radius;
    final int padH;
    final int padV;

    private final int[] fills = new int[MAX_EXP + 1];
    private final int[] textColors = new int[MAX_EXP + 1];
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint basePaint;
    private final int[] presetSizesPx;

    // lazily built per value: label, laid-out text and its vertical offset inside the tile
    private final String[] labels = new String[32];
    private final StaticLayout[] layouts = new StaticLayout[32];
    private final int[] textTop = new int[32];
    private int layoutTop; // top of the layout returned by the last layoutFor call

    TileStyle(Context context, int size, float radius) {
        this.size = size;
        this.radius = radius;
        this.padH = Math.round(size * 0.10f);
        this.padV = Math.round(size * 0.18f);

        for (int i = 0; i <= MAX_EXP; i++) {
            fills[i] = GetColorInt(context, FILL_ATTRS[i]);
            textColors[i] = GetColorInt(context, TEXT_ATTRS[i]);
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        basePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        basePaint.density = dm.density;
        basePaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        basePaint.setLetterSpacing(-0.02f);

        // same preset table as TextView#setAutoSizeTextTypeUniformWithConfiguration(1, 100, 1, SP)
        float min = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, dm);
        float max = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, dm);
        float step = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, dm);
        int count = (int) Math.floor((max - min) / step) + 1;
        int[] sizes = new int[count];
        int unique = 0;
        for (int i = 0; i < count; i++) {
            int px = Math.round(min + i * step);
            if (unique == 0 || sizes[unique - 1] != px)
                sizes[unique++] = px;
        }
        presetSizesPx = new int[unique];
        System.arraycopy(sizes, 0, presetSizesPx, 0, unique);
    }

    private static int exponent(int number) {
        if (number <= 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(number);
    }

    void drawTile(android.graphics.Canvas canvas, int number, int width, int height) {
        int e = exponent(number);
        int colorIndex = Math.min(e, MAX_EXP);
        fillPaint.setColor(fills[colorIndex]);
        canvas.drawRoundRect(0, 0, width, height, radius, radius, fillPaint);
        if (number <= 0)
            return;
        StaticLayout layout = layoutFor(number, e, colorIndex);
        if (layout == null)
            return;
        int save = canvas.save();
        canvas.translate(padH, layoutTop);
        layout.draw(canvas);
        canvas.restoreToCount(save);
    }

    String label(int number) {
        if (number <= 0) return "";
        int e = exponent(number);
        if ((number & (number - 1)) != 0) return String.valueOf(number);
        String s = labels[e];
        if (s == null) labels[e] = s = String.valueOf(number);
        return s;
    }

    private StaticLayout layoutFor(int number, int e, int colorIndex) {
        boolean cacheable = (number & (number - 1)) == 0;
        if (cacheable && layouts[e] != null) {
            layoutTop = textTop[e];
            return layouts[e];
        }

        String text = label(number);
        int availW = size - 2 * padH;
        int availH = size - 2 * padV;
        if (availW <= 0 || availH <= 0)
            return null;

        TextPaint paint = new TextPaint(basePaint);
        paint.setColor(textColors[colorIndex]);
        paint.setTextSize(largestFittingSize(text, paint, availW, availH));
        StaticLayout layout = build(text, paint, availW);
        // vertically centred like TextView with gravity=center: (box - text) >> 1
        int top = padV + ((availH - layout.getHeight()) >> 1);
        if (cacheable) {
            layouts[e] = layout;
            textTop[e] = top;
        }
        layoutTop = top;
        return layout;
    }

    /** The binary search of TextView's uniform auto-size, including its index bookkeeping. */
    private int largestFittingSize(String text, TextPaint paint, int availW, int availH) {
        int sizesCount = presetSizesPx.length;
        int bestSizeIndex = 0;
        int lowIndex = bestSizeIndex + 1;
        int highIndex = sizesCount - 1;
        while (lowIndex <= highIndex) {
            int currentIndex = (lowIndex + highIndex) / 2;
            if (fits(text, paint, presetSizesPx[currentIndex], availW, availH)) {
                bestSizeIndex = lowIndex;
                lowIndex = currentIndex + 1;
            } else {
                highIndex = currentIndex - 1;
                bestSizeIndex = highIndex;
            }
        }
        return presetSizesPx[Math.max(0, bestSizeIndex)];
    }

    private boolean fits(String text, TextPaint paint, int sizePx, int availW, int availH) {
        paint.setTextSize(sizePx);
        StaticLayout layout = build(text, paint, availW);
        int lines = layout.getLineCount();
        if (lines > 1 || layout.getLineEnd(lines - 1) != text.length())
            return false;
        return layout.getHeight() <= availH;
    }

    @SuppressWarnings("deprecation")
    private static StaticLayout build(String text, TextPaint paint, int width) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder b = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .setLineSpacing(0f, 1f)
                    .setMaxLines(1)
                    .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                b.setUseLineSpacingFromFallbacks(true);
            return b.build();
        }
        return new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
    }

    /**
     * Lays out every label a game can realistically reach (2 .. 131072) up front, when the board
     * is built, so a value appearing for the first time never costs anything mid-animation.
     */
    void prewarm() {
        for (int e = 1; e <= 17; e++)
            layoutFor(1 << e, e, Math.min(e, MAX_EXP));
    }

    int emptyColor() {
        return fills[0];
    }
}
