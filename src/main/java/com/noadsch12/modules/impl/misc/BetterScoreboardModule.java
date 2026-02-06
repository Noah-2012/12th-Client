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
import net.minecraft.item.Items;

public class BetterScoreboardModule extends Module {
    public BetterScoreboardModule() {
        super("Better Scoreboard", "Better Scoreboard", Category.MISC,
            "Improves the Scoreboard with Design and Ordering", Items.OAK_SIGN);
    }
}
