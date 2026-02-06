/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 */

package com.noadsch12.modules.impl.player;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.item.Items;

public class AutoRefillModule extends Module {
    public AutoRefillModule() {
        super(
            "Auto Refill",
            "Auto Refill",
            Category.PLAYER,
            "Automatically refills an item in the hotbar",
            Items.CHEST
        );
    }
}
