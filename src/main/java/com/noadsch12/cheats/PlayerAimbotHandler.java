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

import com.noadsch12.modules.impl.combat.AimbotModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class PlayerAimbotHandler {

    public static void updateAimbot(MinecraftClient client) {

        if (client.player == null || client.world == null) return;

        // require mouse click
        if (AimbotModule.requireClick && !client.options.attackKey.isPressed())
            return;

        double range = AimbotModule.range;
        double fov = AimbotModule.fov;

        List<PlayerEntity> players = client.world.getEntitiesByClass(
                PlayerEntity.class,
                client.player.getBoundingBox().expand(range),
                entity -> {

                    if (!entity.isAlive()) return false;
                    if (entity == client.player) return false;
                    if (!client.player.canSee(entity)) return false;

                    if (AimbotModule.ignoreInvisible && entity.isInvisible())
                        return false;

                    if (AimbotModule.ignoreTeammates && isTeammate(client.player, entity))
                        return false;

                    // FOV check
                    if (!isInFov(client, entity, fov))
                        return false;

                    return true;
                }
        );

        if (players.isEmpty()) return;

        PlayerEntity target;

        // target mode
        if (AimbotModule.targetMode == 1) {
            target = players.stream()
                    .min(Comparator.comparingDouble(PlayerEntity::getHealth))
                    .orElse(null);
        } else {
            target = players.stream()
                    .min(Comparator.comparingDouble(p -> p.distanceTo(client.player)))
                    .orElse(null);
        }

        facePlayer(client, target);
    }

    private static boolean isTeammate(PlayerEntity self, PlayerEntity other) {
        Team t1 = self.getScoreboardTeam();
        Team t2 = other.getScoreboardTeam();
        return t1 != null && t1 == t2;
    }

    private static boolean isInFov(MinecraftClient client, PlayerEntity target, double fov) {

        Vec3d eyes = client.player.getEyePos();
        Vec3d look = client.player.getRotationVec(1.0f).normalize();
        Vec3d dir = target.getEyePos().subtract(eyes).normalize();

        double angle = Math.toDegrees(Math.acos(look.dotProduct(dir)));
        return angle <= (fov / 2.0);
    }

    private static void facePlayer(MinecraftClient client, PlayerEntity target) {

        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = client.player.getEyePos();

        double diffX = targetPos.x - playerPos.x;
        double diffY = targetPos.y - playerPos.y;
        double diffZ = targetPos.z - playerPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float smooth = (float) (AimbotModule.smoothness / 20.0);

        float newYaw = lerp(client.player.getYaw(), targetYaw, smooth);
        float newPitch = lerp(client.player.getPitch(), targetPitch, smooth);

        client.player.setYaw(newYaw);
        client.player.setPitch(newPitch);
    }

    private static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }
}
