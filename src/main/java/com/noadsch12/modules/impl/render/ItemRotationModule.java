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

package com.noadsch12.modules.impl.render;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Items;

public class ItemRotationModule extends Module {
    public static float rotation_axis_x = 45.0f;
    public static float rotation_axis_y = 0.0f;
    public static float xOffset = 0.0f;
    public static float yOffset = -0.2f;
    public static float zOffset = 0.5f;

    public ItemRotationModule() {
        super("Item Rotation", "Item Rotation", Category.RENDER,
                "Lets you set the Items position and rotation in hand", Items.TOTEM_OF_UNDYING);
    }

    @Override
    public GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Item Rotation Settings", 50, 50);

        window.setDimensions(240, 220);

        int y = 4;

        // --- Rotation Section ---
        window.addLabel("Rotation", 4, y);
        y += 10;

        window.addSeparator(y);
        y += 6;

        window.addSlider("Axis X", 4, y, 180, -180f, 180f, rotation_axis_x,
                v -> rotation_axis_x = v.floatValue());
        y += 18;

        window.addSlider("Axis Y", 4, y, 180, -180f, 180f, rotation_axis_y,
                v -> rotation_axis_y = v.floatValue());
        y += 22;

        // --- Offset Section ---
        window.addLabel("Offsets", 4, y);
        y += 10;

        window.addSeparator(y);
        y += 6;

        window.addSlider("X Offset", 4, y, 180, -2f, 2f, xOffset,
                v -> xOffset = v.floatValue());
        y += 18;

        window.addSlider("Y Offset", 4, y, 180, -2f, 2f, yOffset,
                v -> yOffset = v.floatValue());
        y += 18;

        window.addSlider("Z Offset", 4, y, 180, -2f, 2f, zOffset,
                v -> zOffset = v.floatValue());
        y += 18;

        window.setVisible(false);
        return window;
    }
}
