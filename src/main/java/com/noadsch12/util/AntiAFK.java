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

package com.noadsch12.util;

import net.minecraft.client.MinecraftClient;

public class AntiAFK {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    public static boolean enabled = false;
    private long lastJumpTime = 0;
    private final long JUMP_INTERVAL = 5000; // 5 Seconds in ms

    public void onTick() {
        if (!enabled || MC.player == null) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastJumpTime >= JUMP_INTERVAL) {
            // Check if the player is on the ground before jumping
            if (MC.player.isOnGround()) {
                MC.player.jump();
                lastJumpTime = currentTime;
            }
        }
    }

    @Deprecated
    public void toggle() {
        this.enabled = !this.enabled;
        // Reset timer when toggled on so it doesn't jump immediately
        if (this.enabled) {
            lastJumpTime = System.currentTimeMillis();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}