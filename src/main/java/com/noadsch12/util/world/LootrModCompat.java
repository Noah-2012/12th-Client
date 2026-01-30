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

package com.noadsch12.util.world;

import net.minecraft.block.entity.BlockEntity;

/**
 * Detects block entities from the Lootr mod for use in ChestESP.
 *
 * <p>
 * Last tested with lootr-fabric-1.21.8-1.16.39.95.
 */
public enum LootrModCompat
{
    ;

    private static final Class<?> lootrBarrelClass = getClassIfExists(
            "noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity");
    private static final Class<?> lootrShulkerBoxClass = getClassIfExists(
            "noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity");
    private static final Class<?> lootrTrappedChestClass = getClassIfExists(
            "noobanidus.mods.lootr.common.block.entity.LootrTrappedChestBlockEntity");

    public static boolean isLootrBarrel(BlockEntity blockEntity)
    {
        if (lootrBarrelClass == null) return false;
        return lootrBarrelClass.isInstance(blockEntity);
    }

    public static boolean isLootrShulkerBox(BlockEntity blockEntity)
    {
        if (lootrShulkerBoxClass == null) return false;
        return lootrShulkerBoxClass.isInstance(blockEntity);
    }

    public static boolean isLootrTrappedChest(BlockEntity blockEntity)
    {
        if (lootrTrappedChestClass == null) return false;
        return lootrTrappedChestClass.isInstance(blockEntity);
    }

    private static Class<?> getClassIfExists(String name)
    {
        try
        {
            return Class.forName(name, false,
                    LootrModCompat.class.getClassLoader());
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }
}
