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

package com.noadsch12.render.esp;

import com.noadsch12.modules.ModuleManager;
import com.noadsch12.render.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RenderESP {

    // --- Block cache ---
    private static final Map<BlockPos, float[]> cachedBlocks = new ConcurrentHashMap<>();
    private static ChunkPos lastChunkPos = null;

    // --- Player cache ---
    private static final Map<UUID, Vec3d> cachedPlayers = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register(
                (BlockEntity be, ClientWorld world) -> updateSingleBlock(be.getPos(), be.getCachedState()));

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(
                (BlockEntity be, ClientWorld world) -> cachedBlocks.remove(be.getPos()));

        WorldRenderEvents.AFTER_ENTITIES.register(RenderESP::render);

        initialized = true;
    }

    // -------------------------------------------------------------------------
    // Core render
    // -------------------------------------------------------------------------
    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        if (!ModuleManager.getInstance().getModule("Storage ESP").isEnabled()
                && !ModuleManager.getInstance().getModule("Player ESP").isEnabled()) return;

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MatrixStack matrices = context.matrices();
        Camera camera        = context.gameRenderer().getCamera();
        Vec3d cameraPos      = camera.getCameraPos();
        Vec3d tracerOrigin   = RenderUtils.getTracerOrigin(camera);

        RenderUtils.beginLines(2.0f, true);

        VertexConsumer lineBuf = consumers.getBuffer(RenderUtils.LINES_NO_DEPTH);

        // --- Storage ESP ---
        if (ModuleManager.getInstance().getModule("Storage ESP").isEnabled()) {
            ChunkPos currentChunk = new ChunkPos(client.player.getBlockPos());
            if (!currentChunk.equals(lastChunkPos)) {
                lastChunkPos = currentChunk;
                scanChunk(client, currentChunk);
            }

            final double MAX_DIST_SQ = 150.0 * 150.0;
            cachedBlocks.forEach((pos, rgb) -> {
                if (pos.getSquaredDistance(cameraPos) <= MAX_DIST_SQ) {
                    RenderUtils.drawBlockBox(matrices, lineBuf, cameraPos, pos, rgb, true);
                    drawTracer(matrices, lineBuf, cameraPos,
                            pos.toCenterPos(), tracerOrigin,
                            rgb[0], rgb[1], rgb[2]);
                }
            });
        }

        // --- Player ESP ---
        if (ModuleManager.getInstance().getModule("Player ESP").isEnabled()) {
            updatePlayerCache(client);
            cachedPlayers.forEach((uuid, eyePos) -> {
                RenderUtils.drawPlayerBox(matrices, lineBuf, cameraPos, eyePos,
                        1f, 0f, 0f, 1f, false);
                drawTracer(matrices, lineBuf, cameraPos,
                        eyePos, tracerOrigin,
                        1f, 0f, 0f);
            });
        }

        RenderUtils.endLines();
    }

    // -------------------------------------------------------------------------
    // Tracer line — ESP-specific (origin → target from the camera's view dir)
    // -------------------------------------------------------------------------
    private static void drawTracer(MatrixStack matrices, VertexConsumer buf,
                                   Vec3d camera, Vec3d target, Vec3d origin,
                                   float r, float g, float b) {
        matrices.push();
        // Both points expressed in camera-relative space — no translate needed
        // because emitLine works directly with the model matrix at identity offset
        org.joml.Matrix4f model = matrices.peek().getPositionMatrix();

        float ox = (float)(origin.x - camera.x);
        float oy = (float)(origin.y - camera.y);
        float oz = (float)(origin.z - camera.z);
        float tx = (float)(target.x - camera.x);
        float ty = (float)(target.y - camera.y);
        float tz = (float)(target.z - camera.z);

        RenderUtils.emitLine(buf, model, ox, oy, oz, tx, ty, tz, r, g, b, 1f);
        matrices.pop();
    }

    // -------------------------------------------------------------------------
    // Block scanning / caching
    // -------------------------------------------------------------------------
    private static void scanChunk(MinecraftClient client, ChunkPos chunkPos) {
        if (client.world == null) return;
        BlockPos start = chunkPos.getStartPos();
        int minY = client.world.getBottomY();
        int maxY = minY + client.world.getHeight();
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = minY; y < maxY; y++) {
                    BlockPos look = start.add(x, y, z);
                    updateSingleBlock(look, client.world.getBlockState(look));
                }
    }

    private static void updateSingleBlock(BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        float[] rgb = null;

        if      (b == Blocks.CHEST)                  rgb = RenderUtils.COLOR_CHEST;
        else if (b == Blocks.TRAPPED_CHEST)          rgb = RenderUtils.COLOR_TRAPPED;
        else if (b == Blocks.BARREL)                 rgb = RenderUtils.COLOR_CHEST;
        else if (b == Blocks.ENDER_CHEST)            rgb = RenderUtils.COLOR_ENDER_CHEST;
        else if (b instanceof ShulkerBoxBlock)       rgb = RenderUtils.COLOR_CHEST;
        else if (b instanceof AbstractFurnaceBlock
                || b instanceof DispenserBlock
                || b instanceof HopperBlock
                || b == Blocks.DROPPER)              rgb = RenderUtils.COLOR_MACHINE;

        if (rgb != null) cachedBlocks.put(pos, rgb);
        else             cachedBlocks.remove(pos);
    }

    // -------------------------------------------------------------------------
    // Player cache
    // -------------------------------------------------------------------------
    private static void updatePlayerCache(MinecraftClient client) {
        cachedPlayers.clear();
        assert client.world != null;
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            cachedPlayers.put(player.getUuid(), player.getCameraPosVec(1f));
        }
    }
}