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

package com.noadsch12.cheats;

import com.noadsch12.modules.impl.combat.AutoTotemModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem {

    private static long lastSwapTime = 0;

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.interactionManager == null) return;

        // --------------------------------------------------
        // DELAY HANDLING
        // --------------------------------------------------
        if (AutoTotemModule.delayEnabled) {
            long now = System.currentTimeMillis();
            if (now - lastSwapTime < AutoTotemModule.delay) return;
        }

        // Slot 45 is offhand
        ItemStack offhandStack = player.getOffHandStack();

        // Already holding totem
        if (offhandStack.getItem() == Items.TOTEM_OF_UNDYING) return;

        int totemSlot = findTotemSlot(player.getInventory());
        if (totemSlot == -1) return;

        int syncId = player.playerScreenHandler.syncId;

        // Pick up totem
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, player);

        // Place in offhand
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, player);

        // Return previous offhand item
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, player);

        lastSwapTime = System.currentTimeMillis();
    }

    private static int findTotemSlot(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                if (i < 9) return i + 36; // hotbar → handler slot
                return i;                 // main inventory
            }
        }
        return -1;
    }
}
