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

package com.noadsch12.handlers;

import com.noadsch12.modules.impl.combat.AutoArmorModule; // <-- import your module
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoArmor {

    private static long lastEquipTime = 0;

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null) return;

        // ---------------------------------------
        // DELAY HANDLING
        // ---------------------------------------
        if (AutoArmorModule.delayEnabled) {
            long now = System.currentTimeMillis();
            if (now - lastEquipTime < AutoArmorModule.delay) return;
        }

        boolean equipped = false;

        // Slot IDs: 5 (Head), 6 (Chest), 7 (Legs), 8 (Feet)
        equipped |= checkAndEquip(player, 5, EquipmentSlot.HEAD);
        equipped |= checkAndEquip(player, 6, EquipmentSlot.CHEST);
        equipped |= checkAndEquip(player, 7, EquipmentSlot.LEGS);
        equipped |= checkAndEquip(player, 8, EquipmentSlot.FEET);

        if (equipped) {
            lastEquipTime = System.currentTimeMillis();
        }
    }

    private static boolean checkAndEquip(ClientPlayerEntity player, int handlerSlot, EquipmentSlot targetSlot) {
        ItemStack currentArmor = player.playerScreenHandler.getSlot(handlerSlot).getStack();

        // Only equip if empty
        if (!currentArmor.isEmpty()) return false;

        int bestSlot = findBestArmorInInventory(player, targetSlot);
        if (bestSlot != -1) {
            int syncId = player.playerScreenHandler.syncId;
            MinecraftClient.getInstance().interactionManager.clickSlot(
                    syncId,
                    bestSlot,
                    0,
                    SlotActionType.QUICK_MOVE,
                    player
            );
            return true;
        }

        return false;
    }

    private static int findBestArmorInInventory(ClientPlayerEntity player, EquipmentSlot targetSlot) {
        List<Integer> candidates = new ArrayList<>();

        // Main inventory slots 9 to 44
        for (int i = 9; i < 45; i++) {
            ItemStack stack = player.playerScreenHandler.getSlot(i).getStack();

            var equippable = stack.get(DataComponentTypes.EQUIPPABLE);
            if (equippable != null && equippable.slot() == targetSlot) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty()) return -1;

        Comparator<Integer> comparator = Comparator.comparingInt(slot ->
                player.playerScreenHandler.getSlot(slot).getStack()
                        .getOrDefault(DataComponentTypes.DAMAGE, 0)
        );

        // ---------------------------------------
        // WEAKEST FIRST OPTION
        // ---------------------------------------
        if (AutoArmorModule.weakestFirst) {
            // highest damage first (lowest durability)
            return candidates.stream().max(comparator).orElse(-1);
        } else {
            // lowest damage first (best durability)
            return candidates.stream().min(comparator).orElse(-1);
        }
    }
}
