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

package com.noadsch12.modules.impl.movement;
import com.noadsch12.event.events.TickEvent;
import com.noadsch12.event.listeners.TickListener;
import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class AntiAFKModule extends Module implements TickListener {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    public static boolean enabled = false;
    private long lastJumpTime = 0;

    public AntiAFKModule() {
        super("Anti AFK", "Anti AFK", Category.MOVEMENT,
            "Lets the Player jump every five Seconds", Items.CLOCK);
    }

    @Override
    public void onTick(TickEvent.Post event) {
        if (!enabled || MC.player == null) return;

        long currentTime = System.currentTimeMillis();

        // 5 Seconds in ms
        long JUMP_INTERVAL = 5000;
        if (currentTime - lastJumpTime >= JUMP_INTERVAL) {
            // Check if the player is on the ground before jumping
            if (MC.player.isOnGround()) {
                MC.player.jump();
                lastJumpTime = currentTime;
            }
        }
    }

    @Override
    public void toggle() {
        enabled = !enabled;

        if (enabled) {
            lastJumpTime = System.currentTimeMillis();
        }
    }


    @Override
    public void onTick(TickEvent.Pre event) {

    }
}
