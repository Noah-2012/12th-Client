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

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.Comparator;

public class PlayerAimbotHandler {
    private static final double RANGE = 8.0;

    public static void updateAimbot(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Find the closest PlayerEntity within 8 blocks
        PlayerEntity target = client.world.getEntitiesByClass(
                        PlayerEntity.class,
                        client.player.getBoundingBox().expand(RANGE),
                        entity -> entity.isAlive()
                                && entity != client.player // IMPORTANT: Don't aim at yourself!
                                && client.player.canSee(entity)
                ).stream()
                .min(Comparator.comparingDouble(player -> player.distanceTo(client.player)))
                .orElse(null);

        if (target != null) {
            facePlayer(client, target);
        }
    }

    private static void facePlayer(MinecraftClient client, PlayerEntity target) {
        // Targets the eye position or center of the player
        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = client.player.getEyePos();

        double diffX = targetPos.x - playerPos.x;
        double diffY = targetPos.y - playerPos.y;
        double diffZ = targetPos.z - playerPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        client.player.setYaw(yaw);
        client.player.setPitch(pitch);
    }
}