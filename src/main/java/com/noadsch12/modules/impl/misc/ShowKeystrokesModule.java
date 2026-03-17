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

package com.noadsch12.modules.impl.misc;
import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.render.ui.keystrokes.KeystrokesConfig;
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Items;

public class ShowKeystrokesModule extends Module {
    public ShowKeystrokesModule() {
        super("Show Keystrokes", "Show Keystrokes", Category.MISC,
            "Shows Keystrokes with Options like CPS, WASD and Mouse buttons", Items.ANVIL);
    }

    @Override
    protected GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Keystrokes Settings", 50, 50);

        window.setDimensions(200, 90);

        int y = 4;

        window.addCheckbox("Spacebar", 4, y, true, state -> {
            KeystrokesConfig.showSpace = state;
        });
        y += 14;

        window.addCheckbox("Mouse Buttons", 4, y, true, state -> {
            KeystrokesConfig.showMouseButtons = state;
        });
        y += 14;

        window.addCheckbox("CPS Display", 4, y, true, state -> {
            KeystrokesConfig.showCPS = state;
        });
        y += 14;

        window.addSlider("HUD Scale", 4, y, 100, 0.5, 1.5, 1.0, value -> {
            KeystrokesConfig.scale = value;
        });

        window.setVisible(false);

        return window;
    }
}
