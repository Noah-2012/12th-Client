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

package com.noadsch12.render.ui;

public class DebugAnimation {
    public static float progress = 0f;
    public static boolean isF3Visible = false;
    public static boolean hasBeenActivated = false;

    private static long lastTime = System.currentTimeMillis();
    private static final float SPEED = 4.0f; // 4.0f = 0.25 Sekunden für die Animation

    public static void update() {
        long currentTime = System.currentTimeMillis();
        // Zeit seit dem letzten Frame in Sekunden (z.B. 0.016 für 60 FPS)
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        if (isF3Visible) {
            progress = Math.min(1f, progress + SPEED * deltaTime);
        } else {
            progress = Math.max(0f, progress - SPEED * deltaTime);
        }
    }

    public static boolean shouldActuallyRender() {
        return isF3Visible || progress > 0.001f;
    }

    public static float getEasedProgress() {
        return (float) Math.sin(progress * Math.PI / 2);
    }
}
