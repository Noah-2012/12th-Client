/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 */

package com.noadsch12.modules.impl.misc;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.item.Items;

public class JumpToFoodModule extends Module {
    public JumpToFoodModule() {
        super(
            "Jump to Food",
            "Jump to Food",
            Category.MISC,
            "Automatically jumps to next food item in hotbar",
            Items.BREAD
        );
    }
}
