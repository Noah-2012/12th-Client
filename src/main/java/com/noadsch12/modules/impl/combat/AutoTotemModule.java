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

package com.noadsch12.modules.impl.combat;
import com.noadsch12.event.EventBus;
import com.noadsch12.event.events.PlayerHealthEvent;
import com.noadsch12.event.listeners.PlayerHealthListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

public class AutoTotemModule extends Module implements PlayerHealthListener {
    public AutoTotemModule() {
        super("Auto Totem", "Auto Totem", Category.COMBAT,
            "Automatically places the totem in the slot", Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void onEnable() {
        EventBus.register(this);
    }

    @Override
    public void onDisable() {
        EventBus.unregister(this);
    }

    @Override
    public void onHealthChanged(PlayerHealthEvent readPacketEvent) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null) return; // <-- add this
        if (mc.currentScreen instanceof HandledScreen) return;

        PlayerInventory inventory = mc.player.getInventory();
        ItemStack handItemStack = inventory.getSelectedStack();

        // If offhand already has a totem, skip
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        if (handItemStack.getItem() == Items.TOTEM_OF_UNDYING) return;

        if (readPacketEvent.getHealth() <= 5) {
            SwitchToTotem();
        }
    }

    private void SwitchToTotem() {
        MinecraftClient mc = MinecraftClient.getInstance();

        PlayerInventory inventory = mc.player.getInventory();

        int slot = -1;
        for (int i = 0; i <= 36; i++) {
            ItemStack itemStackToCheck = inventory.getStack(i);
            Item itemToCheck = itemStackToCheck.getItem();

            if (itemToCheck == Items.TOTEM_OF_UNDYING) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            /*
            mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId,
                    slot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );

             */
            mc.player.setStackInHand(Hand.OFF_HAND, inventory.getStack(slot));
        }
    }
}
