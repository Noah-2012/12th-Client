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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Stealth {
    private static boolean enabled = false;
    // Flag to tell our Mixin to let this packet through
    public static boolean isSendingStealthPacket = false;

    private static Vec3d lastPos = Vec3d.ZERO;
    private static float lastYaw = 0;
    private static float lastPitch = 0;
    private static boolean lastOnGround = true;
    private static boolean lastHorizontalCollision = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (state && !enabled && client.player != null) {
            captureState(client.player);
        } else if (!state && enabled && client.player != null) {
            captureState(client.player);
        }
        enabled = state;
    }

    private static void captureState(ClientPlayerEntity player) {
        lastPos = player.getEntityPos();
        lastYaw = player.getYaw();
        lastPitch = player.getPitch();
        lastOnGround = player.isOnGround();
        lastHorizontalCollision = false;
    }

    public static void onTick(MinecraftClient client) {
        if (enabled && client.getNetworkHandler() != null && client.player != null) {
            isSendingStealthPacket = true;

            // Matching the 7-argument constructor found in your source for Full:
            // Full(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean horizontalCollision)
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.Full(
                    lastPos.x,
                    lastPos.y,
                    lastPos.z,
                    lastYaw,
                    lastPitch,
                    lastOnGround,
                    lastHorizontalCollision
            );

            client.getNetworkHandler().sendPacket(packet);

            isSendingStealthPacket = false;
        }
    }
}