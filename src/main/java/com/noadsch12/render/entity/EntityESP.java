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

package com.noadsch12.render.entity;

import com.noadsch12.modules.impl.combat.EntityEspModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class EntityESP {

    public static void render(Camera camera, VertexConsumerProvider vertexConsumers, Matrix4f positionMatrix) {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d cameraPos = camera.getPos();

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);

        VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double maxDistSq = EntityEspModule.maxDistance * EntityEspModule.maxDistance;

        for (Entity entity : mc.world.getEntities()) {

            if (!EntityEspModule.showSelf && entity == mc.player) continue;
            if (entity.squaredDistanceTo(mc.player) > maxDistSq) continue;

            if (!shouldRender(entity)) continue;

            Box box = entity.getBoundingBox();
            float[] color = getEntityColor(entity);

            drawBox(lines, matrix, box, cameraPos, color[0], color[1], color[2]);
        }

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(RenderLayer.getLines());
        }
    }

    private static boolean shouldRender(Entity entity) {

        if (entity instanceof PlayerEntity)
            return EntityEspModule.showPlayers;

        if (entity.getType().getSpawnGroup().isPeaceful())
            return EntityEspModule.showPassive;

        return EntityEspModule.showHostile;
    }

    private static float[] getEntityColor(Entity entity) {

        if (!EntityEspModule.useCustomColors) {
            if (entity instanceof PlayerEntity) return new float[]{1,0,0};
            if (entity.getType().getSpawnGroup().isPeaceful()) return new float[]{0,1,0};
            return new float[]{1,1,0};
        }

        if (entity instanceof PlayerEntity)
            return new float[]{
                    (float)EntityEspModule.playerR,
                    (float)EntityEspModule.playerG,
                    (float)EntityEspModule.playerB
            };

        if (entity.getType().getSpawnGroup().isPeaceful())
            return new float[]{
                    (float)EntityEspModule.passiveR,
                    (float)EntityEspModule.passiveG,
                    (float)EntityEspModule.passiveB
            };

        return new float[]{
                (float)EntityEspModule.hostileR,
                (float)EntityEspModule.hostileG,
                (float)EntityEspModule.hostileB
        };
    }


    private static void drawBox(VertexConsumer buffer, Matrix4f matrix, Box box, Vec3d cam, float r, float g, float b) {
        float x1 = (float)(box.minX - cam.x);
        float y1 = (float)(box.minY - cam.y);
        float z1 = (float)(box.minZ - cam.z);
        float x2 = (float)(box.maxX - cam.x);
        float y2 = (float)(box.maxY - cam.y);
        float z2 = (float)(box.maxZ - cam.z);

        // Bottom
        line(buffer, matrix, x1, y1, z1, x2, y1, z1, r, g, b);
        line(buffer, matrix, x2, y1, z1, x2, y1, z2, r, g, b);
        line(buffer, matrix, x2, y1, z2, x1, y1, z2, r, g, b);
        line(buffer, matrix, x1, y1, z2, x1, y1, z1, r, g, b);
        // Top
        line(buffer, matrix, x1, y2, z1, x2, y2, z1, r, g, b);
        line(buffer, matrix, x2, y2, z1, x2, y2, z2, r, g, b);
        line(buffer, matrix, x2, y2, z2, x1, y2, z2, r, g, b);
        line(buffer, matrix, x1, y2, z2, x1, y2, z1, r, g, b);
        // Verticals
        line(buffer, matrix, x1, y1, z1, x1, y2, z1, r, g, b);
        line(buffer, matrix, x2, y1, z1, x2, y2, z1, r, g, b);
        line(buffer, matrix, x2, y1, z2, x2, y2, z2, r, g, b);
        line(buffer, matrix, x1, y1, z2, x1, y2, z2, r, g, b);
    }

    private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, 1f).normal(0, 1, 0);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, 1f).normal(0, 1, 0);
    }
}