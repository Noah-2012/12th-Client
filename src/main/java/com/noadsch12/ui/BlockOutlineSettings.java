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