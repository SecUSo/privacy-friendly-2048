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
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

import org.secuso.privacyfriendly2048.helpers.SpringInterpolator;

/**
 * Spring based tile motion, tuned to feel like iOS:
 * <ul>
 *     <li>slide: a (nearly) critically damped spring, fast start and a long soft landing</li>
 *     <li>merge: the arriving tile pops (~14%) and rings out with a small undershoot; the tile it
 *     absorbs travels to the meeting point underneath it and disappears when it lands</li>
 *     <li>spawn: new tiles grow from 40% with a slight overshoot while fading in</li>
 * </ul>
 * Each tile owns one reusable animator per kind of motion, so a move allocates nothing.
 * The slide integrates the spring itself instead of using an interpolator: when a new swipe
 * redirects a tile that is still travelling, the new spring starts with the tile's current
 * velocity, so fast swipes chain without the tile stopping and starting again. A tile whose
 * destination did not change simply keeps its course.
 * From rest, the slide curve is identical to {@code SpringInterpolator(0.95, 0.16, SLIDE_MS)}.
 */
final class TileAnimations {

    /** Slide duration; spawns and the jump of emptied tiles wait for it. */
    static final long SLIDE_MS = 180;
    static final long POP_MS = 340;
    static final long SPAWN_MS = 300;
    private static final long SPAWN_FADE_MS = 150;

    private static final float POP_AMPLITUDE = 0.14f;
    private static final float SPAWN_FROM_SCALE = 0.4f;

    private static final TimeInterpolator LINEAR = new LinearInterpolator();
    private static final TimeInterpolator POP = new SpringInterpolator.Pulse(0.45, 0.30, POP_MS);
    private static final TimeInterpolator SPAWN = new SpringInterpolator(0.68, 0.32, SPAWN_MS);
    private static final TimeInterpolator FADE = new PathInterpolator(0.2f, 0f, 0f, 1f);

    private static final float Z_MOVING = 1f;
    private static final float Z_MERGING = 2f;
    private static final float Z_ABSORBED = 0.5f;

    private TileAnimations() {
    }

    /**
     * Displacement from the target of a damped spring (damping 0.95, response 0.16 s) with an
     * arbitrary start offset and velocity, corrected to land exactly on the target after SLIDE_MS.
     */
    static final class Spring {
        private static final double ZETA = 0.95;
        private static final double OMEGA0 = 2.0 * Math.PI / 0.16;
        private static final double DECAY = ZETA * OMEGA0;
        private static final double OMEGA_D = OMEGA0 * Math.sqrt(1.0 - ZETA * ZETA);
        static final double DURATION = SLIDE_MS / 1000.0;

        private double a;
        private double b;
        private double end;

        /** @param d0 offset from the target in px, @param v0 velocity in px/s */
        void set(double d0, double v0) {
            a = d0;
            b = (v0 + DECAY * d0) / OMEGA_D;
            end = raw(DURATION);
        }

        private double raw(double t) {
            return Math.exp(-DECAY * t) * (a * Math.cos(OMEGA_D * t) + b * Math.sin(OMEGA_D * t));
        }

        double offset(double t) {
            if (t >= DURATION) return 0.0;
            return raw(t) - end * (t / DURATION);
        }

        double velocity(double t) {
            if (t >= DURATION) return 0.0;
            double env = Math.exp(-DECAY * t);
            double c = Math.cos(OMEGA_D * t);
            double s = Math.sin(OMEGA_D * t);
            return env * ((-DECAY * a + OMEGA_D * b) * c + (-DECAY * b - OMEGA_D * a) * s) - end / DURATION;
        }
    }

    // ------------------------------------------------------------------------------------------
    // public entry points

    /**
     * Slides a tile to its slot.
     *
     * @param merge the tile absorbs another one when it arrives
     */
    static void slide(Element e, boolean merge) {
        animateTo(e, e.dPosX, e.dPosY, merge, merge ? Z_MERGING : Z_MOVING);
    }

    /**
     * A tile that is absorbed in this move: it travels to the meeting point underneath the tile
     * that absorbs it and jumps (hidden) to its own slot once the sliding tiles have arrived.
     */
    static void absorb(Element e) {
        Element by = e.victimOf;
        if (by != null) {
            float tx = by.dPosX;
            float ty = by.dPosY;
            if (e.getX() != tx || e.getY() != ty || isRunning(e.moveAnimator))
                animateTo(e, tx, ty, false, Z_ABSORBED);
        }
        jumpAfterSlide(e);
    }

    /**
     * Moves a tile that is not visibly travelling (an emptied slot) to its slot once the sliding
     * tiles have arrived.
     */
    static void jumpAfterSlide(Element e) {
        if (e.jumpPending)
            return; // already scheduled; it reads the latest slot when it fires
        ValueAnimator a = jump(e);
        e.jumpPending = true;
        e.jumpCancelled = false;
        a.start();
    }

    /**
     * Completes what is left over from the previous move before a new one is laid out:
     * pending jumps happen now, so every tile that is not visibly travelling sits in its slot.
     * Travelling tiles are left alone and get redirected smoothly.
     */
    static void flush(Element e) {
        if (!e.jumpPending)
            return;
        e.jumpPending = false;
        if (e.jumpAnimator != null)
            e.jumpAnimator.cancel();
        completeJump(e);
    }

    /** Springy scale pulse for a tile that just absorbed another one. */
    static void pop(Element e) {
        if (isRunning(e.spawnAnimator))
            e.spawnAnimator.end();
        ValueAnimator a = popAnimator(e);
        if (a.isStarted())
            a.cancel();
        e.setScaleX(1f);
        e.setScaleY(1f);
        a.start();
    }

    /**
     * New tile: after {@code delay} it grows in with a soft overshoot and fades in. Until then the
     * tile keeps showing whatever it showed before (e.g. the tile it is absorbing slides in on top).
     */
    static void spawn(Element e, long delay) {
        ValueAnimator a = spawnAnimator(e);
        if (a.isStarted())
            a.cancel();
        if (isRunning(e.popAnimator))
            e.popAnimator.cancel();
        // the value counts as shown for merge detection, as if it had been drawn already
        e.dNumber = e.number;
        e.spawnPending = true;
        a.setStartDelay(delay);
        a.start();
    }

    /** Stops everything on a tile and puts it in its final resting state. */
    static void settle(Element e) {
        if (isRunning(e.moveAnimator)) e.moveAnimator.cancel();
        if (isRunning(e.jumpAnimator)) e.jumpAnimator.cancel();
        if (isRunning(e.popAnimator)) e.popAnimator.cancel();
        if (isRunning(e.spawnAnimator)) e.spawnAnimator.cancel();
        e.jumpPending = false;
        e.spawnPending = false;
        e.moveMerge = false;
        e.victimOf = null;
        e.victimShow = 0;
        e.setTranslationZ(0f);
        e.setAlpha(1f);
        e.setScaleX(1f);
        e.setScaleY(1f);
    }

    // ------------------------------------------------------------------------------------------
    // slide

    private static void animateTo(Element e, float tx, float ty, boolean merge, float z) {
        ValueAnimator a = moveAnimator(e);
        double vx = 0.0;
        double vy = 0.0;
        if (a.isStarted()) {
            if (e.moveTargetX == tx && e.moveTargetY == ty) {
                // same destination: keep the current course instead of starting over
                if (merge) e.moveMerge = true;
                if (e.getTranslationZ() < z) e.setTranslationZ(z);
                return;
            }
            double t = a.getAnimatedFraction() * Spring.DURATION;
            vx = e.springX.velocity(t);
            vy = e.springY.velocity(t);
            a.cancel();
        }
        e.springX.set(e.getX() - tx, vx);
        e.springY.set(e.getY() - ty, vy);
        e.moveTargetX = tx;
        e.moveTargetY = ty;
        e.moveMerge = merge;
        e.setTranslationZ(z);
        e.moveCancelled = false;
        a.start();
    }

    private static void onLanded(Element e) {
        e.setX(e.moveTargetX);
        e.setY(e.moveTargetY);
        e.setTranslationZ(0f);
        if (e.jumpPending) {
            // absorbed: wait under the arriving tile until the jump hides it
            if (e.victimShow != 0)
                e.paint(e.victimShow);
            return;
        }
        boolean merged = e.moveMerge;
        e.moveMerge = false;
        show(e);
        if (merged) {
            pop(e);
            if (e.onMerge != null) e.onMerge.run();
        }
    }

    private static void completeJump(Element e) {
        if (isRunning(e.moveAnimator))
            e.moveAnimator.cancel();
        e.setX(e.dPosX);
        e.setY(e.dPosY);
        e.setTranslationZ(0f);
        e.victimOf = null;
        e.victimShow = 0;
        show(e);
    }

    /** Shows the logical value, but keeps a tile whose spawn has not started yet hidden. */
    private static void show(Element e) {
        e.drawItem();
        if (e.spawnPending && e.getVisibility() == View.VISIBLE)
            e.setVisibility(View.INVISIBLE);
    }

    // ------------------------------------------------------------------------------------------
    // reusable animators, created on first use

    private static ValueAnimator moveAnimator(final Element e) {
        if (e.moveAnimator != null)
            return e.moveAnimator;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(SLIDE_MS);
        a.setInterpolator(LINEAR);
        a.addUpdateListener(animation -> {
            double t = animation.getAnimatedFraction() * Spring.DURATION;
            e.setX((float) (e.moveTargetX + e.springX.offset(t)));
            e.setY((float) (e.moveTargetY + e.springY.offset(t)));
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                e.moveCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!e.moveCancelled)
                    onLanded(e);
            }
        });
        e.moveAnimator = a;
        return a;
    }

    private static ValueAnimator jump(final Element e) {
        if (e.jumpAnimator != null)
            return e.jumpAnimator;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(0);
        a.setStartDelay(SLIDE_MS);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                e.jumpCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (e.jumpCancelled || !e.jumpPending)
                    return;
                e.jumpPending = false;
                completeJump(e);
            }
        });
        e.jumpAnimator = a;
        return a;
    }

    private static ValueAnimator popAnimator(final Element e) {
        if (e.popAnimator != null)
            return e.popAnimator;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(POP_MS);
        a.setInterpolator(LINEAR);
        a.addUpdateListener(animation -> {
            float s = 1f + POP_AMPLITUDE * POP.getInterpolation(animation.getAnimatedFraction());
            e.setScaleX(s);
            e.setScaleY(s);
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                e.setScaleX(1f);
                e.setScaleY(1f);
            }
        });
        e.popAnimator = a;
        return a;
    }

    private static ValueAnimator spawnAnimator(final Element e) {
        if (e.spawnAnimator != null)
            return e.spawnAnimator;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(SPAWN_MS);
        a.setInterpolator(LINEAR);
        a.addUpdateListener(animation -> {
            if (e.spawnPending) {
                e.spawnPending = false;
                if (e.number == 0) {
                    // absorbed by another tile before it even appeared
                    animation.cancel();
                    return;
                }
                if (!isRunning(e.moveAnimator)) {
                    flush(e);
                    e.setX(e.dPosX);
                    e.setY(e.dPosY);
                }
                e.drawItem();
            }
            float f = animation.getAnimatedFraction();
            float s = SPAWN_FROM_SCALE + (1f - SPAWN_FROM_SCALE) * SPAWN.getInterpolation(f);
            e.setScaleX(s);
            e.setScaleY(s);
            e.setAlpha(FADE.getInterpolation(Math.min(1f, f * SPAWN_MS / (float) SPAWN_FADE_MS)));
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                e.spawnPending = false;
                e.setAlpha(1f);
                e.setScaleX(1f);
                e.setScaleY(1f);
            }
        });
        e.spawnAnimator = a;
        return a;
    }

    private static boolean isRunning(ValueAnimator a) {
        return a != null && a.isStarted();
    }
}
