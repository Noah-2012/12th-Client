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
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Items;

public class BetterChatModule extends Module {
    public static boolean showPlayerHeads = true;
    public static boolean showAnimations = true;

    public BetterChatModule() {
        super("Better Chat", "Better Chat", Category.MISC,
            "Improves the Chat in many ways", Items.WRITABLE_BOOK);
    }

    @Override
    public GLWindow createSettingsWindow() {
        int y = 4;

        GLWindow window = new GLWindow("Better Chat Settings", 50, 50);

        window.setDimensions(200, 60);

        window.addCheckbox("Player Heads", 4, y, showPlayerHeads, state -> {
            showPlayerHeads = state;
        });

        y += 14;

        window.addCheckbox("Animations", 4, y, showAnimations, state -> {
            showAnimations = state;
        });

        window.setVisible(false);

        return window;
    }
}
