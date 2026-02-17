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

public class EntityEspModule extends Module {

    // ===== FILTERS =====
    public static boolean showPlayers = true;
    public static boolean showHostile = true;
    public static boolean showPassive = true;
    public static boolean showSelf = false;

    // ===== RENDER =====
    public static double maxDistance = 64;
    public static double lineWidth = 1.5;

    // ===== COLOR CONTROL =====
    public static boolean useCustomColors = false;

    public static double playerR = 1.0;
    public static double playerG = 0.0;
    public static double playerB = 0.0;

    public static double hostileR = 1.0;
    public static double hostileG = 1.0;
    public static double hostileB = 0.0;

    public static double passiveR = 0.0;
    public static double passiveG = 1.0;
    public static double passiveB = 0.0;

    public EntityEspModule() {
        super("Entity ESP", "Entity ESP", Category.COMBAT,
                "Draws hitboxes around entities", Items.ENDER_PEARL);
    }

    @Override
    public GLWindow createSettingsWindow() {

        GLWindow window = new GLWindow("Entity ESP Settings", 50, 50);
        window.setDimensions(240, 440);

        int y = 4;

        window.addLabel("Entity Filters", 4, y);
        y += 10;
        window.addSeparator(y);
        y += 6;

        window.addCheckbox("Show Players", 4, y, showPlayers, v -> showPlayers = v);
        y += 14;

        window.addCheckbox("Show Hostile", 4, y, showHostile, v -> showHostile = v);
        y += 14;

        window.addCheckbox("Show Passive", 4, y, showPassive, v -> showPassive = v);
        y += 14;

        window.addCheckbox("Show Yourself", 4, y, showSelf, v -> showSelf = v);
        y += 18;

        window.addLabel("Rendering", 4, y);
        y += 10;
        window.addSeparator(y);
        y += 6;

        window.addSlider("Max Distance", 4, y, 150, 8, 256, maxDistance,
                v -> maxDistance = v);
        y += 20;

        window.addSlider("Line Width", 4, y, 150, 0.5, 5, lineWidth,
                v -> lineWidth = v);
        y += 20;

        window.addCheckbox("Use Custom Colors", 4, y, useCustomColors,
                v -> useCustomColors = v);
        y += 18;

        window.addLabel("Player Color", 4, y);
        y += 12;
        window.addSlider("R", 4, y, 120, 0, 1, playerR, v -> playerR = v);
        y += 20;
        window.addSlider("G", 4, y, 120, 0, 1, playerG, v -> playerG = v);
        y += 20;
        window.addSlider("B", 4, y, 120, 0, 1, playerB, v -> playerB = v);
        y += 20;

        window.addLabel("Hostile Color", 4, y);
        y += 12;
        window.addSlider("R ", 4, y, 120, 0, 1, hostileR, v -> hostileR = v);
        y += 20;
        window.addSlider("G ", 4, y, 120, 0, 1, hostileG, v -> hostileG = v);
        y += 20;
        window.addSlider("B ", 4, y, 120, 0, 1, hostileB, v -> hostileB = v);
        y += 20;

        window.addLabel("Passive Color", 4, y);
        y += 12;
        window.addSlider("R  ", 4, y, 120, 0, 1, passiveR, v -> passiveR = v);
        y += 20;
        window.addSlider("G  ", 4, y, 120, 0, 1, passiveG, v -> passiveG = v);
        y += 20;
        window.addSlider("B  ", 4, y, 120, 0, 1, passiveB, v -> passiveB = v);

        window.setVisible(false);
        return window;
    }
}
