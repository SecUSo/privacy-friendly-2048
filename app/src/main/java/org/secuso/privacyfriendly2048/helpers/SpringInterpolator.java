/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 */

package org.secuso.privacyfriendly2048.helpers;

import android.animation.TimeInterpolator;

/**
 * A damped spring evaluated in closed form, so it can drive a normal fixed-duration animator.
 * <p>
 * Parameters follow the iOS / SwiftUI convention {@code spring(response:dampingFraction:)}:
 * {@code response} is the period of the undamped oscillation in seconds, {@code dampingRatio}
 * is 1.0 for a critically damped spring (no overshoot) and below 1.0 for a bouncy one.
 * The curve is nudged so that it lands exactly on 1.0 at the end of {@code durationMs}.
 */
public final class SpringInterpolator implements TimeInterpolator {

    private final double zeta;
    private final double omega0;
    private final double omegaD;
    private final double durationSec;
    private final double endError;

    public SpringInterpolator(double dampingRatio, double responseSec, long durationMs) {
        zeta = Math.max(0.05, Math.min(dampingRatio, 1.0));
        omega0 = 2.0 * Math.PI / responseSec;
        omegaD = omega0 * Math.sqrt(Math.max(0.0, 1.0 - zeta * zeta));
        durationSec = Math.max(1, durationMs) / 1000.0;
        endError = position(durationSec) - 1.0;
    }

    /** Position of a unit step response at time t (seconds). */
    private double position(double t) {
        double envelope = Math.exp(-zeta * omega0 * t);
        if (zeta >= 0.999) {
            return 1.0 - envelope * (1.0 + omega0 * t);
        }
        return 1.0 - envelope * (Math.cos(omegaD * t) + (zeta * omega0 / omegaD) * Math.sin(omegaD * t));
    }

    @Override
    public float getInterpolation(float input) {
        if (input <= 0f) return 0f;
        if (input >= 1f) return 1f;
        return (float) (position(input * durationSec) - endError * input);
    }

    /**
     * The impulse response of a damped spring: starts at 0, rises to 1, rings out and settles at 0.
     * Used for "pop" effects, e.g. {@code scale = 1 + amplitude * pulse(t)}.
     */
    public static final class Pulse implements TimeInterpolator {
        private final double zeta;
        private final double omega0;
        private final double omegaD;
        private final double durationSec;
        private final double peak;

        public Pulse(double dampingRatio, double responseSec, long durationMs) {
            zeta = Math.max(0.05, Math.min(dampingRatio, 0.95));
            omega0 = 2.0 * Math.PI / responseSec;
            omegaD = omega0 * Math.sqrt(1.0 - zeta * zeta);
            durationSec = Math.max(1, durationMs) / 1000.0;
            double tPeak = Math.atan(omegaD / (zeta * omega0)) / omegaD;
            peak = raw(tPeak);
        }

        private double raw(double t) {
            return Math.exp(-zeta * omega0 * t) * Math.sin(omegaD * t);
        }

        @Override
        public float getInterpolation(float input) {
            if (input <= 0f || input >= 1f) return 0f;
            // fade the (already tiny) tail out so the curve ends exactly at rest
            double fade = 1.0 - Math.pow(input, 4);
            return (float) (raw(input * durationSec) / peak * fade);
        }
    }
}
