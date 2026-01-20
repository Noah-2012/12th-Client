package com.noadsch12.cheats;

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

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null) return;

        // Slot IDs: 5 (Head), 6 (Chest), 7 (Legs), 8 (Feet)
        checkAndEquip(player, 5, EquipmentSlot.HEAD);
        checkAndEquip(player, 6, EquipmentSlot.CHEST);
        checkAndEquip(player, 7, EquipmentSlot.LEGS);
        checkAndEquip(player, 8, EquipmentSlot.FEET);
    }

    private static void checkAndEquip(ClientPlayerEntity player, int handlerSlot, EquipmentSlot targetSlot) {
        // Check what is currently in the armor slot
        ItemStack currentArmor = player.playerScreenHandler.getSlot(handlerSlot).getStack();

        // Only equip if the slot is empty
        if (!currentArmor.isEmpty()) return;

        int bestSlot = findBestArmorInInventory(player, targetSlot);
        if (bestSlot != -1) {
            int syncId = player.playerScreenHandler.syncId;
            MinecraftClient.getInstance().interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.QUICK_MOVE, player);
        }
    }

    private static int findBestArmorInInventory(ClientPlayerEntity player, EquipmentSlot targetSlot) {
        List<Integer> candidates = new ArrayList<>();

        // Main inventory slots 9 to 44
        for (int i = 9; i < 45; i++) {
            ItemStack stack = player.playerScreenHandler.getSlot(i).getStack();

            // In 1.21.10, we check the Equippable Component
            // This is the new way since ArmorItem is being phased out
            var equippable = stack.get(DataComponentTypes.EQUIPPABLE);
            if (equippable != null && equippable.slot() == targetSlot) {
                candidates.add(i);
            }
        }

        // Return the one with the highest damage (lowest durability)
        return candidates.stream()
                .max(Comparator.comparingInt(slot ->
                        player.playerScreenHandler.getSlot(slot).getStack().getOrDefault(DataComponentTypes.DAMAGE, 0)))
                .orElse(-1);
    }
}