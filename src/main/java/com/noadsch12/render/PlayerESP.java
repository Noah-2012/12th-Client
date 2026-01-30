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

package com.noadsch12.render;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerESP {

    private static final Map<UUID, Vec3d> cachedPlayers = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
    }

    public static void render(DrawContext context) {
        if (!ClientSettingsScreen.PlayerESPEnabled) return;
        init();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        updatePlayerCache(client);
        renderAll(context, client);
    }

    private static void updatePlayerCache(MinecraftClient client) {
        cachedPlayers.clear();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue; // Don't render yourself

            // IMPORTANT: Use eye position, not feet
            cachedPlayers.put(player.getUuid(), player.getCameraPosVec(1.0F));
        }
    }

    private static void renderAll(DrawContext context, MinecraftClient client) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float fov = (float) client.options.getFov().getValue();
        Matrix4f projMat = client.gameRenderer.getBasicProjectionMatrix(fov);
        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Matrix4f viewMat = new Matrix4f().rotation(camera.getRotation().conjugate());

        cachedPlayers.forEach((uuid, pos) -> {
            int color = pack(255, 0, 0, 255); // Red for players
            renderTarget(context, client, pos, camPos, projMat, viewMat, sw, sh, color);
        });
    }

    private static void renderTarget(DrawContext context, MinecraftClient client, Vec3d target,
                                     Vec3d camPos, Matrix4f proj, Matrix4f view, int sw, int sh, int color) {
        float dx = (float) (target.x - camPos.x);
        float dy = (float) (target.y - camPos.y);
        float dz = (float) (target.z - camPos.z);

        Vector4f pos = new Vector4f(dx, dy, dz, 1.0f);
        pos.mul(view).mul(proj);

        if (pos.w <= 0) return;

        float screenX = ((pos.x / pos.w) + 1.0f) * sw / 2.0f;
        float screenY = (1.0f - (pos.y / pos.w)) * sh / 2.0f;

        drawTracer(context, sw / 2f, sh / 2f, screenX, screenY, color);

        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(screenX, screenY);

        double dist = target.distanceTo(camPos);
        int s = (int) MathHelper.clamp(40.0 / (dist * 0.4), 3, 20);

        context.fill(-s, -s, s, -s + 1, color);
        context.fill(-s, s - 1, s, s, color);
        context.fill(-s, -s + 1, -s + 1, s - 1, color);
        context.fill(s - 1, -s + 1, s, s - 1, color);

        stack.popMatrix();
    }

    private static void drawTracer(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1, dy = y2 - y1;
        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(x1, y1);
        stack.rotate((float) Math.atan2(dy, dx));
        context.fill(0, 0, (int) Math.sqrt(dx * dx + dy * dy), 1, color);
        stack.popMatrix();
    }

    private static int pack(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
