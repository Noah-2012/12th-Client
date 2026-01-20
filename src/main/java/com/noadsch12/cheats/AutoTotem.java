package com.noadsch12.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem {

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.interactionManager == null) return;

        // Slot 45 is the Offhand (Shield) slot
        ItemStack offhandStack = player.getOffHandStack();

        // If we already have a totem in the offhand, do nothing
        if (offhandStack.getItem() == Items.TOTEM_OF_UNDYING) return;

        // Find a totem in the main inventory
        int totemSlot = findTotemSlot(player.getInventory());

        if (totemSlot != -1) {
            // Inventory slots in ScreenHandler are numbered differently than PlayerInventory
            // For the main player inventory, slot 0-8 are Hotbar, 9-35 are Main.
            // In the PlayerScreenHandler, slot 45 is offhand.

            int syncId = player.playerScreenHandler.syncId;

            // Step 1: Click the totem slot to "pick it up" (put it on the cursor)
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, player);

            // Step 2: Click the offhand slot (45) to "place it"
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, player);

            // Step 3: If we had something in the offhand before, it's now on the cursor.
            // Put it back into the old totem slot.
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, player);
        }
    }

    private static int findTotemSlot(PlayerInventory inventory) {
        // We check the main inventory (0-35)
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                // Map inventory index to ScreenHandler slot index
                // Minecraft's ScreenHandler inventory slots start at 9 (after armor/crafting)
                if (i < 9) {
                    return i + 36; // Hotbar slots are 36-44 in the handler
                }
                return i; // Main inventory slots 9-35 match
            }
        }
        return -1;
    }
}