package com.fury.tiktoksample.animations.animation;

import android.animation.TimeInterpolator;

/**
 * Created by danielzeller on 09.04.15.
 */
public class ExpoOut implements TimeInterpolator {
    @Override
    public float getInterpolation(float t) {
        return (t == 1) ? 1 : -(float) Math.pow(2, -10 * t) + 1;
    }
}