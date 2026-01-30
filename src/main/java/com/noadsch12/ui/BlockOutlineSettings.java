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

package com.noadsch12.ui;

import java.awt.Color;

public class BlockOutlineSettings {
    public static boolean enabled = true;
    public static float r = 1.0f;
    public static float g = 0.5f;
    public static float b = 1.0f;
    public static float a = 1.0f;
    public static boolean rainbow = false;
    public static float rainbowSpeed = 5.0f;

    public static Color getOutlineColor() {
        if (rainbow) {
            float hue = (System.currentTimeMillis() % (int)(10000 / rainbowSpeed)) / (10000 / rainbowSpeed);
            return Color.getHSBColor(hue, 0.8f, 1.0f);
        }
        return new Color(r, g, b, 1.0f);
    }
}