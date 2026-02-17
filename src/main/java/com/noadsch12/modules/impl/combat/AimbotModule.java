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

package com.noadsch12.modules.impl.combat;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.List;

public class AimbotModule extends Module {
    public static double range = 6.0;
    public static double fov = 180.0;
    public static double smoothness = 5.0;

    public static boolean requireClick = false;
    public static boolean ignoreInvisible = true;
    public static boolean ignoreTeammates = true;

    // 0 = nearest, 1 = lowest health
    public static int targetMode = 0;

    private static final List<String> TARGET_OPTIONS =
            Arrays.asList("Nearest", "Lowest Health");

    public AimbotModule() {
        super("Aimbot", "Aimbot", Category.COMBAT,
                "Always moves the crosshair to the nearest Player", Items.BOW);
    }

    @Override
    public GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Aimbot Settings", 50, 50);

        window.setDimensions(220, 370);

        int y = 4;

        window.addLabel("Targeting", 4, y);
        y += 10;

        window.addSeparator(y);
        y += 6;

        window.addSlider("Range", 4, y, 150, 1, 12, range, v -> range = v);
        y += 18;

        window.addSlider("FOV", 4, y, 150, 10, 360, fov, v -> fov = v);
        y += 18;

        window.addSlider("Smoothness", 4, y, 150, 1, 20, smoothness, v -> smoothness = v);
        y += 18;

        window.addDropdown("Target Mode", 4, y, 120, TARGET_OPTIONS, targetMode,
                index -> targetMode = index);
        y += 18;

        window.addSeparator(y);
        y += 8;

        window.addCheckbox("Require Mouse Click", 4, y, requireClick,
                state -> requireClick = state);
        y += 14;

        window.addCheckbox("Ignore Invisible", 4, y, ignoreInvisible,
                state -> ignoreInvisible = state);
        y += 14;

        window.addCheckbox("Ignore Teammates", 4, y, ignoreTeammates,
                state -> ignoreTeammates = state);

        window.setVisible(false);
        return window;
    }
}
