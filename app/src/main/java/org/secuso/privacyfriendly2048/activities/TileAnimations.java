/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 */

package org.secuso.privacyfriendly2048.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.PathInterpolator;

import org.secuso.privacyfriendly2048.helpers.SpringInterpolator;

/**
 * Spring based tile motion, tuned to feel like iOS:
 * <ul>
 *     <li>slide: a critically damped spring, fast start and a long soft landing</li>
 *     <li>merge: the arriving tile pops (~14%) and rings out with a small undershoot</li>
 *     <li>spawn: new tiles grow from 40% with a slight overshoot while fading in</li>
 * </ul>
 * Every animator is tracked on its {@link Element}, so a new swipe that arrives mid-animation
 * continues smoothly from where the tile currently is, and a cancelled animation always leaves
 * the tile in a consistent state (right position, right number, scale and alpha back at 1).
 * Positions are always read from the element's current target (dPosX/dPosY) when an animation
 * finishes, so a late callback can never move a tile to a stale place.
 */
final class TileAnimations {

    /** Slide duration; spawns and the "teleport" of emptied tiles wait for it. */
    static final long SLIDE_MS = 180;
    static final long POP_MS = 340;
    static final long SPAWN_MS = 300;
    private static final long SPAWN_FADE_MS = 150;

    private static final float POP_AMPLITUDE = 0.14f;
    private static final float SPAWN_FROM_SCALE = 0.4f;

    private static final TimeInterpolator SLIDE = new SpringInterpolator(0.95, 0.16, SLIDE_MS);
    private static final TimeInterpolator POP = new SpringInterpolator.Pulse(0.45, 0.30, POP_MS);
    private static final TimeInterpolator SPAWN = new SpringInterpolator(0.68, 0.32, SPAWN_MS);
    private static final TimeInterpolator FADE = new PathInterpolator(0.2f, 0f, 0f, 1f);

    private TileAnimations() {
    }

    /**
     * Slides a tile from wherever it is now to its target position.
     *
     * @param merge   the tile absorbs another one when it arrives
     * @param onMerge run once the merging tile has landed (may be null)
     */
    static void slide(final Element e, final boolean merge, final Runnable onMerge) {
        cancel(e.jumpAnimator);
        cancel(e.moveAnimator);

        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(e,
                PropertyValuesHolder.ofFloat(View.X, e.dPosX),
                PropertyValuesHolder.ofFloat(View.Y, e.dPosY));
        a.setDuration(SLIDE_MS);
        a.setInterpolator(SLIDE);
        // moving tiles are drawn above resting ones, the merging tile above everything
        e.setTranslationZ(merge ? 2f : 1f);
        a.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (e.moveAnimator == animation) e.moveAnimator = null;
                e.setTranslationZ(0f);
                if (!cancelled) {
                    e.setX(e.dPosX);
                    e.setY(e.dPosY);
                }
                e.drawItem();
                if (merge && !cancelled) {
                    pop(e);
                    if (onMerge != null) onMerge.run();
                }
            }
        });
        e.moveAnimator = a;
        a.start();
    }

    /**
     * Moves a tile that is not visibly travelling (an emptied slot, or the tile that is being
     * absorbed) to its target once the sliding tiles have arrived.
     */
    static void jumpAfterSlide(final Element e) {
        cancel(e.jumpAnimator);
        cancel(e.moveAnimator);

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(0);
        a.setStartDelay(SLIDE_MS);
        a.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (e.jumpAnimator == animation) e.jumpAnimator = null;
                if (cancelled) return;
                e.setX(e.dPosX);
                e.setY(e.dPosY);
                e.drawItem();
            }
        });
        e.jumpAnimator = a;
        a.start();
    }

    /** Springy scale pulse for a tile that just absorbed another one. */
    static void pop(final Element e) {
        finish(e.spawnAnimator);
        cancel(e.popAnimator);
        e.setScaleX(1f);
        e.setScaleY(1f);

        float peak = 1f + POP_AMPLITUDE;
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(e,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, peak),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, peak));
        a.setDuration(POP_MS);
        a.setInterpolator(POP);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (e.popAnimator == animation) e.popAnimator = null;
                e.setScaleX(1f);
                e.setScaleY(1f);
            }
        });
        e.popAnimator = a;
        a.start();
    }

    /** New tile: grows in with a soft overshoot and fades in, after the slide has finished. */
    static void spawn(final Element e, long delay) {
        cancel(e.popAnimator);
        cancel(e.spawnAnimator);
        e.setAlpha(0f);
        e.setScaleX(SPAWN_FROM_SCALE);
        e.setScaleY(SPAWN_FROM_SCALE);

        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(e,
                PropertyValuesHolder.ofFloat(View.SCALE_X, SPAWN_FROM_SCALE, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, SPAWN_FROM_SCALE, 1f));
        scale.setDuration(SPAWN_MS);
        scale.setInterpolator(SPAWN);

        ObjectAnimator fade = ObjectAnimator.ofFloat(e, View.ALPHA, 0f, 1f);
        fade.setDuration(SPAWN_FADE_MS);
        fade.setInterpolator(FADE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scale, fade);
        set.setStartDelay(delay);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (e.spawnAnimator == animation) e.spawnAnimator = null;
                e.setAlpha(1f);
                e.setScaleX(1f);
                e.setScaleY(1f);
            }
        });
        e.spawnAnimator = set;
        set.start();
    }

    /** Stops everything on a tile and puts it in its final resting state. */
    static void settle(Element e) {
        cancel(e.moveAnimator);
        cancel(e.jumpAnimator);
        cancel(e.popAnimator);
        cancel(e.spawnAnimator);
        e.setTranslationZ(0f);
        e.setAlpha(1f);
        e.setScaleX(1f);
        e.setScaleY(1f);
    }

    private static void cancel(Animator a) {
        if (a != null) a.cancel();
    }

    private static void finish(Animator a) {
        if (a != null) a.end();
    }
}
