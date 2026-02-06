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

package com.noadsch12.modules;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Categories for organizing modules in the UI
 */
public enum Category {
    PLAYER("Player", Items.PLAYER_HEAD, 0xFFFFAA00),
    MISC("Misc", Items.COMPASS, 0xFFFFAA00),
    RENDER("Render", Items.SPYGLASS, 0xFFFFAA00),
    COMBAT("Combat", Items.NETHERITE_SWORD, 0xFFFFAA00),
    MOVEMENT("Movement", Items.ELYTRA, 0xFFFFAA00);

    private final String displayName;
    private final Item iconItem;
    private final int color;

    Category(String displayName, Item iconItem, int color) {
        this.displayName = displayName;
        this.iconItem = iconItem;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Item getIconItem() {
        return iconItem;
    }

    public int getColor() {
        return color;
    }

    /**
     * Get the X position for this category's column in the UI
     */
    public int getColumnX(int centerX, int columnWidth) {
        return switch (this) {
            case PLAYER -> centerX - 424;
            case MISC -> centerX - 250;
            case RENDER -> centerX - (columnWidth / 2);
            case COMBAT -> centerX + 100;
            case MOVEMENT -> centerX + 274;
        };
    }
}