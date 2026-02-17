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

public class AutoTotemModule extends Module {
    public static boolean delayEnabled = true;
    public static double delay = 100;

    public AutoTotemModule() {
        super("Auto Totem", "Auto Totem", Category.COMBAT,
            "Automatically places the totem in the slot", Items.TOTEM_OF_UNDYING);
    }

    @Override
    public GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Auto Armor Settings", 50, 50);

        window.setDimensions(200, 90);

        int y = 4;

        window.addCheckbox("Enable Delay", 4, y, delayEnabled, state -> {
            delayEnabled = state;
        });

        y += 14;

        window.addSlider("Delay (ms)", 4, y, 150, 0, 300, delay, value -> {
            delay = value;
        });

        window.setVisible(false);

        return window;
    }
}
