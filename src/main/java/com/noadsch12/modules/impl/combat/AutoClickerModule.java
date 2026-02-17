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

public class AutoClickerModule extends Module {
    public static boolean delayEnabled = false;
    public static double delay = 50;

    public static boolean holdOnly = true;

    // 0 = LMB, 1 = RMB
    public static int mouseButton = 0;

    private static final List<String> BUTTON_OPTIONS =
            Arrays.asList("LMB", "RMB");

    public AutoClickerModule() {
        super("Auto Clicker", "Auto Clicker", Category.COMBAT,
                "Auto clicks the mouse", Items.IRON_SWORD);
    }

    @Override
    public GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Auto Clicker Settings", 50, 50);

        window.setDimensions(200, 130);

        int y = 4;

        window.addCheckbox("Enable Delay", 4, y, delayEnabled, state -> {
            delayEnabled = state;
        });

        y += 14;

        window.addSlider("Delay (ms)", 4, y, 150, 0, 300, delay, value -> {
            delay = value;
        });

        y += 14;

        window.addCheckbox("Only Click When Holding", 4, y, holdOnly, state -> {
            holdOnly = state;
        });

        y += 16;

        window.addDropdown(
                "Mouse Button",
                4,
                y,
                80,
                BUTTON_OPTIONS,
                mouseButton,
                index -> mouseButton = index
        );

        window.setVisible(false);
        return window;
    }
}
