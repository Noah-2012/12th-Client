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

package com.noadsch12.modules.impl.movement;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Items;

public class BaritoneModule extends Module {

    public BaritoneModule() {
        super("Baritone Module", "Baritone Module", Category.MOVEMENT,
                "A Module for testing the Baritone API", Items.IRON_INGOT);
    }

    /** Called when the module is toggled on */
    @Override
    protected void onEnable() {
        // TODO: implement onEnable
    }

    /** Called when the module is toggled off */
    @Override
    protected void onDisable() {
        // TODO: implement onDisable
    }

    /* Override for an optional Settings Screen */
    @Override
    protected GLWindow createSettingsWindow() {
        GLWindow window = new GLWindow("Baritone Settings", 50, 50);
        window.setDimensions(240, 150); // Breite x Höhe

        int y = 4;



        window.setVisible(false); // Fenster standardmäßig unsichtbar
        return window;
    }
}
