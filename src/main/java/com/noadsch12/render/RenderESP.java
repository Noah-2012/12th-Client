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

import com.noadsch12.modules.ModuleManager;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RenderESP {

    // Persistent storage: Blocks are only removed if broken/unloaded
    private static final Map<BlockPos, Integer> cachedBlocks = new ConcurrentHashMap<>();
    private static ChunkPos lastChunkPos = null;

    // Player cache
    private static final Map<UUID, Vec3d> cachedPlayers = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        // Adds blocks to cache when they load into the world
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity blockEntity, ClientWorld world) -> updateSingleBlock(blockEntity.getPos(), blockEntity.getCachedState()));

        // Removes blocks ONLY when they are actually removed/unloaded
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity blockEntity, ClientWorld world) -> cachedBlocks.remove(blockEntity.getPos()));

        initialized = true;
    }

    public static void render(DrawContext context) {
        // Only run if at least one ESP is enabled
        if (!ModuleManager.getInstance().getModule("Storage ESP").isEnabled() && !ModuleManager.getInstance().getModule("Player ESP").isEnabled()) return;

        init();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        // Shared Variables: Matrices calculated once for both features
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float fov = (float) client.options.getFov().getValue();
        Matrix4f projMat = client.gameRenderer.getBasicProjectionMatrix(fov);
        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Matrix4f viewMat = new Matrix4f().rotation(camera.getRotation().conjugate());

        // --- Chest ESP Logic ---
        if (ModuleManager.getInstance().getModule("Storage ESP").isEnabled()) {
            // Detect if player moved to a new chunk
            ChunkPos currentChunk = new ChunkPos(client.player.getBlockPos());
            if (!currentChunk.equals(lastChunkPos)) {
                lastChunkPos = currentChunk;
                // Only scan the current chunk you just entered to fill in gaps
                scanChunk(client, currentChunk);
            }

            // Add distance culling to reduce lag - only render blocks within 150 blocks
            final double MAX_DISTANCE = 150.0;
            cachedBlocks.forEach((pos, color) -> {
                double dist = Math.sqrt(pos.getSquaredDistance(camPos));
                if (dist <= MAX_DISTANCE) {
                    renderChestTarget(context, pos, camPos, projMat, viewMat, sw, sh, color);
                }
            });
        }

        // --- Player ESP Logic ---
        if (ModuleManager.getInstance().getModule("Player ESP").isEnabled()) {
            updatePlayerCache(client);
            cachedPlayers.forEach((uuid, pos) -> {
                int color = pack(255, 0, 0, 255); // Red for players
                renderPlayerTarget(context, client, pos, camPos, projMat, viewMat, sw, sh, color);
            });
        }
    }


    // --- Original ChestESP Methods ---

    private static void scanChunk(MinecraftClient client, ChunkPos chunkPos) {
        if (client.world == null) return;

        BlockPos start = chunkPos.getStartPos();

        // Use getBottomY and getHeight to define the loop range without using Heightmaps
        int minY = client.world.getBottomY();
        int maxY = minY + client.world.getHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    // We use the temporary BlockPos.Mutable to reduce object creation lag
                    BlockPos look = start.add(x, y, z);
                    updateSingleBlock(look, client.world.getBlockState(look));
                }
            }
        }
    }

    private static void updateSingleBlock(BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        int color = -1;

        if (b == Blocks.CHEST) color = pack(255, 160, 0);
        else if (b == Blocks.TRAPPED_CHEST) color = pack(255, 0, 0);
        else if (b == Blocks.BARREL) color = pack(255, 160, 0);
        else if (b == Blocks.ENDER_CHEST) color = pack(120, 0, 255);
        else if (b instanceof ShulkerBoxBlock) color = pack(255, 160, 0);
        else if (b instanceof AbstractFurnaceBlock || b instanceof DispenserBlock ||
                b instanceof HopperBlock || b == Blocks.DROPPER) {
            color = pack(140, 140, 140);
        }

        if (color != -1) {
            cachedBlocks.put(pos, color);
        }
    }

    private static void renderChestTarget(DrawContext context, BlockPos blockPos,
                                          Vec3d camPos, Matrix4f proj, Matrix4f view, int sw, int sh, int color) {

        // Define the 8 corners of the block bounding box
        float minX = blockPos.getX();
        float minY = blockPos.getY();
        float minZ = blockPos.getZ();
        float maxX = minX + 1.0f;
        float maxY = minY + 1.0f;
        float maxZ = minZ + 1.0f;

        // Calculate all 8 corners in world space relative to camera
        Vector3f[] corners = new Vector3f[8];
        corners[0] = new Vector3f((float)(minX - camPos.x), (float)(minY - camPos.y), (float)(minZ - camPos.z));
        corners[1] = new Vector3f((float)(maxX - camPos.x), (float)(minY - camPos.y), (float)(minZ - camPos.z));
        corners[2] = new Vector3f((float)(maxX - camPos.x), (float)(minY - camPos.y), (float)(maxZ - camPos.z));
        corners[3] = new Vector3f((float)(minX - camPos.x), (float)(minY - camPos.y), (float)(maxZ - camPos.z));
        corners[4] = new Vector3f((float)(minX - camPos.x), (float)(maxY - camPos.y), (float)(minZ - camPos.z));
        corners[5] = new Vector3f((float)(maxX - camPos.x), (float)(maxY - camPos.y), (float)(minZ - camPos.z));
        corners[6] = new Vector3f((float)(maxX - camPos.x), (float)(maxY - camPos.y), (float)(maxZ - camPos.z));
        corners[7] = new Vector3f((float)(minX - camPos.x), (float)(maxY - camPos.y), (float)(maxZ - camPos.z));

        // Project all corners to screen space
        Vector4f[] screenCorners = new Vector4f[8];
        boolean allBehind = true;

        for (int i = 0; i < 8; i++) {
            Vector4f pos = new Vector4f(corners[i].x, corners[i].y, corners[i].z, 1.0f);
            pos.mul(view).mul(proj);
            screenCorners[i] = pos;

            if (pos.w > 0) {
                allBehind = false;
            }
        }

        // Don't render if all corners are behind the camera
        if (allBehind) return;

        // Convert to screen coordinates
        float[][] screenPos = new float[8][2];
        for (int i = 0; i < 8; i++) {
            if (screenCorners[i].w > 0) {
                screenPos[i][0] = ((screenCorners[i].x / screenCorners[i].w) + 1.0f) * sw / 2.0f;
                screenPos[i][1] = (1.0f - (screenCorners[i].y / screenCorners[i].w)) * sh / 2.0f;
            } else {
                screenPos[i][0] = screenCorners[i].x > 0 ? sw * 2 : -sw;
                screenPos[i][1] = screenCorners[i].y > 0 ? -sh : sh * 2;
            }
        }

        // Draw all 12 edges of the cube
        // Bottom face
        drawLine(context, screenPos[0][0], screenPos[0][1], screenPos[1][0], screenPos[1][1], color);
        drawLine(context, screenPos[1][0], screenPos[1][1], screenPos[2][0], screenPos[2][1], color);
        drawLine(context, screenPos[2][0], screenPos[2][1], screenPos[3][0], screenPos[3][1], color);
        drawLine(context, screenPos[3][0], screenPos[3][1], screenPos[0][0], screenPos[0][1], color);

        // Top face
        drawLine(context, screenPos[4][0], screenPos[4][1], screenPos[5][0], screenPos[5][1], color);
        drawLine(context, screenPos[5][0], screenPos[5][1], screenPos[6][0], screenPos[6][1], color);
        drawLine(context, screenPos[6][0], screenPos[6][1], screenPos[7][0], screenPos[7][1], color);
        drawLine(context, screenPos[7][0], screenPos[7][1], screenPos[4][0], screenPos[4][1], color);

        // Vertical edges
        drawLine(context, screenPos[0][0], screenPos[0][1], screenPos[4][0], screenPos[4][1], color);
        drawLine(context, screenPos[1][0], screenPos[1][1], screenPos[5][0], screenPos[5][1], color);
        drawLine(context, screenPos[2][0], screenPos[2][1], screenPos[6][0], screenPos[6][1], color);
        drawLine(context, screenPos[3][0], screenPos[3][1], screenPos[7][0], screenPos[7][1], color);

        // Draw tracer from center of screen to center of box
        Vec3d blockCenter = blockPos.toCenterPos();
        Vector4f centerPos = new Vector4f(
                (float)(blockCenter.x - camPos.x),
                (float)(blockCenter.y - camPos.y),
                (float)(blockCenter.z - camPos.z),
                1.0f
        );
        centerPos.mul(view).mul(proj);

        if (centerPos.w > 0) {
            float centerX = ((centerPos.x / centerPos.w) + 1.0f) * sw / 2.0f;
            float centerY = (1.0f - (centerPos.y / centerPos.w)) * sh / 2.0f;
            drawLine(context, sw / 2f, sh / 2f, centerX, centerY, color);
        }
    }

    private static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        // Don't draw extremely long lines (likely wrapping around screen)
        if (length > 2000) return;

        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(x1, y1);
        stack.rotate((float) Math.atan2(dy, dx));
        context.fill(0, 0, (int) length, 1, color);
        stack.popMatrix();
    }

    private static int pack(int r, int g, int b) {
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    // --- Original PlayerESP Methods ---

    private static void updatePlayerCache(MinecraftClient client) {
        cachedPlayers.clear();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue; // Don't render yourself

            // IMPORTANT: Use eye position, not feet
            cachedPlayers.put(player.getUuid(), player.getCameraPosVec(1.0F));
        }
    }

    private static void renderPlayerTarget(DrawContext context, MinecraftClient client, Vec3d target,
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