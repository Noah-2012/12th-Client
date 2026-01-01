package com.noadsch12.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoRefill {

    // Store the last known item in each of the 9 hotbar slots
    private static final Item[] lastHotbarState = new Item[9];

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null) return;

        // Loop through hotbar slots (0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = player.getInventory().getStack(i);
            Item currentItem = currentStack.getItem();

            // If the slot is now empty, but used to have an item
            if (currentStack.isEmpty() && lastHotbarState[i] != null && lastHotbarState[i] != Items.AIR) {
                refillSlot(player, i, lastHotbarState[i]);
            }

            // Update the "memory" for the next tick
            lastHotbarState[i] = currentItem;
        }
    }

    private static void refillSlot(ClientPlayerEntity player, int hotbarSlot, Item itemToTarget) {
        // Search main inventory (slots 9 to 35)
        // Note: PlayerInventory indices 9-35 match the ScreenHandler indices 9-35
        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.getItem() == itemToTarget) {
                int syncId = player.playerScreenHandler.syncId;
                MinecraftClient mc = MinecraftClient.getInstance();

                // Step 1: Click the item in the main inventory
                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, player);

                // Step 2: Click the empty hotbar slot (Hotbar slots in handler are 36-44)
                int handlerHotbarSlot = hotbarSlot + 36;
                mc.interactionManager.clickSlot(syncId, handlerHotbarSlot, 0, SlotActionType.PICKUP, player);

                // Done refilling for this slot
                return;
            }
        }
    }
}