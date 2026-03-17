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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;

public class ChestStealer {
    private static boolean enabled = false;
    private static boolean isStealingInProgress = false;
    private static long stealStartTime = 0;
    private static int initialDelay = 0;
    private static final Random random = new Random();

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            isStealingInProgress = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void onScreenOpen(ScreenHandler screenHandler) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Check if it's actually a container (chest, barrel, shulker box, etc.)
        if (!(screenHandler instanceof GenericContainerScreenHandler)) {
            return;
        }

        // Reset state for new chest
        isStealingInProgress = true;
        stealStartTime = System.currentTimeMillis();
        initialDelay = 100 + random.nextInt(101); // Random delay between 100-200ms
    }

    public static void tick() {
        if (!enabled || !isStealingInProgress) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || player.currentScreenHandler == null) {
            isStealingInProgress = false;
            return;
        }

        ScreenHandler handler = player.currentScreenHandler;

        // Double-check it's still a container
        if (!(handler instanceof GenericContainerScreenHandler)) {
            isStealingInProgress = false;
            return;
        }

        // Wait for initial delay before starting to steal
        long currentTime = System.currentTimeMillis();
        if (currentTime - stealStartTime < initialDelay) {
            return; // Wait for initial delay
        }

        // Steal all items at once
        int chestSlotCount = handler.slots.size() - 36; // 36 = player inventory slots

        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();

            if (!stack.isEmpty() && shouldTakeItem(stack, player)) {
                // Click the slot to take the item
                assert client.interactionManager != null;
                client.interactionManager.clickSlot(
                        handler.syncId,
                        i,
                        0,
                        SlotActionType.QUICK_MOVE,
                        player
                );
            }
        }

        // Close the screen immediately after stealing
        client.execute(player::closeHandledScreen);
        isStealingInProgress = false;
    }

    private static boolean shouldTakeItem(ItemStack stack, ClientPlayerEntity player) {
        // Check if player already has this item type in inventory
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (!invStack.isEmpty() && ItemStack.areItemsEqual(stack, invStack)) {
                return true; // Take items we already have
            }
        }

        // Check if it's a "good" item (you can customize this logic)
        return isGoodItem(stack);
    }

    private static boolean isGoodItem(ItemStack stack) {
        // Customize this method to define what constitutes a "good" item
        // Example criteria:
        String itemName = stack.getItem().toString().toLowerCase();

        // Skip common junk items
        if (itemName.contains("dirt") ||
                itemName.contains("cobblestone") ||
                itemName.contains("stick")) {
            return false;
        }

        // Take tools, weapons, armor, food, valuable items
        return stack.getItem().getDefaultStack().isDamageable() || // Tools, weapons, armor
                stack.contains(DataComponentTypes.FOOD) ||        // Food items
                stack.getRarity().ordinal() > 0;   // Uncommon+ rarity items
    }
}