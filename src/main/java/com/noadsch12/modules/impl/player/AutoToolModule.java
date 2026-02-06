/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 */

package com.noadsch12.modules.impl.player;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.item.Items;

public class AutoToolModule extends Module {
    public AutoToolModule() {
        super(
            "Auto Tool",
            "Auto Tool",
            Category.PLAYER,
            "Automatically jumps to the most efficient\ntool when performing an action",
            Items.DIAMOND_PICKAXE
        );
    }
}
