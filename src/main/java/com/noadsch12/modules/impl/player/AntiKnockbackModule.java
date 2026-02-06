/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 */

package com.noadsch12.modules.impl.player;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.item.Items;

public class AntiKnockbackModule extends Module {
    public AntiKnockbackModule() {
        super(
                "Anti Knockback",
                "Anti Knockback",
                Category.PLAYER,
                "Removes the knockback by canceling the packet",
                Items.SHIELD
        );
    }
}