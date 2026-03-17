/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.util.math;

import java.util.Random;

public final class MathUtils {

    private static final Random RANDOM = new Random();

    private MathUtils() {} // Prevent instantiation

    /* =========================
       Primitive Conversions
       ========================= */

    public static float doubleToFloat(Double value) {
        return value.floatValue();
    }

    public static int doubleToInt(Double value) {
        return value.intValue();
    }

    public static Double convertDouble(double value) {
        return value;
    }

    public static double convertDouble(Double value) {
        return value.doubleValue();
    }

    public static float intToFloat(int value) {
        return (float) value;
    }

    public static double intToDouble(int value) {
        return (double) value;
    }

    public static int floatToInt(float value) {
        return (int) value;
    }

    public static double floatToDouble(float value) {
        return (double) value;
    }

    /* =========================
       Clamp Methods
       ========================= */

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /* =========================
       Lerp (Linear Interpolation)
       ========================= */

    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }

    public static double lerp(double start, double end, double delta) {
        return start + delta * (end - start);
    }

    /* =========================
       Angle Conversion
       ========================= */

    public static double degToRad(double degrees) {
        return Math.toRadians(degrees);
    }

    public static double radToDeg(double radians) {
        return Math.toDegrees(radians);
    }

    /* =========================
       Rounding
       ========================= */

    public static int floor(double value) {
        return (int) Math.floor(value);
    }

    public static int ceil(double value) {
        return (int) Math.ceil(value);
    }

    public static int round(double value) {
        return (int) Math.round(value);
    }

    /* =========================
       Distance
       ========================= */

    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distance3D(double x1, double y1, double z1,
                                    double x2, double y2, double z2) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /* =========================
       Random
       ========================= */

    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static float randomFloat(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static double randomDouble(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    /* =========================
       Normalize / Map
       ========================= */

    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    public static double map(double value,
                             double inMin, double inMax,
                             double outMin, double outMax) {

        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    /* =========================
       Percentage
       ========================= */

    public static double percent(double value, double max) {
        return (value / max) * 100.0;
    }

}