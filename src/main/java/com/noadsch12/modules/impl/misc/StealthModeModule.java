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
import com.noadsch12.util.Stealth;
import net.minecraft.item.Items;

public class StealthModeModule extends Module {
    public StealthModeModule() {
        super("Stealth Mode", "Stealth Mode", Category.MISC,
            "Completely hides the Actions you do from the Server", Items.ENDER_EYE);
    }

    @Override
    protected void onEnable() {
        Stealth.setEnabled(true);
    }

    @Override
    protected void onDisable() {
        Stealth.setEnabled(false);
    }
}
