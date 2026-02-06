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
import com.noadsch12.util.AntiAFK;
import net.minecraft.item.Items;

public class AntiAFKModule extends Module {
    public AntiAFKModule() {
        super("Anti AFK", "Anti AFK", Category.MOVEMENT,
            "Lets the Player jump every five Seconds", Items.CLOCK);
    }

    @Override
    protected void onEnable() {
        AntiAFK.enabled = true;
    }

    @Override
    protected void onDisable() {
        AntiAFK.enabled = false;
    }
}
