/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 */

package com.noadsch12.render.fx;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noadsch12.TwelfthClient;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;

public class ExplosiveScanner {

    private static final float TNT_RADIUS = 4.0f;
    private static final float CREEPER_RADIUS = 3.0f;
    private static final float CRYSTAL_RADIUS = 6.0f;
    private static final float ANCHOR_RADIUS = 5.0f;
    private static final float MINECART_TNT_RADIUS = 4.0f;

    private static final RenderLayer OVERLAY_LINES = RenderLayer.of(
            "explosive_lines",
            1536,
            false,
            true,
            RenderPipeline.builder()
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withLocation(TwelfthClient.identifier("esp_lines"))
                    .build(),
            RenderLayer.MultiPhaseParameters.builder()
                    .build(false)
    );

    public static void init() {
        // BEFORE_DEBUG_RENDER is the modern way to draw custom world-space lines
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ExplosiveScanner::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        VertexConsumer buffer = consumers.getBuffer(OVERLAY_LINES);

        // 1. Scan for Explosive Entities
        for (Entity entity : client.world.getEntities()) {
            float radius = 0;
            if (entity instanceof TntEntity) {
                radius = TNT_RADIUS;
            } else if (entity instanceof CreeperEntity creeper && creeper.getFuseSpeed() > 0) {
                radius = CREEPER_RADIUS;
            } else if (entity instanceof EndCrystalEntity) {
                radius = CRYSTAL_RADIUS;
            } else if (entity instanceof TntMinecartEntity) {
                radius = MINECART_TNT_RADIUS;
            }

            if (radius > 0) {
                drawDangerZone(context, matrices, buffer, entity.getEntityPos(), radius, client.player.getEntityPos());
            }
        }

        // 2. Scan for Respawn Anchors (Checking a 16-block radius around player)
        BlockPos playerBlockPos = client.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(playerBlockPos.add(-16, -8, -16), playerBlockPos.add(16, 8, 16))) {
            var state = client.world.getBlockState(pos);
            if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                // Anchors only explode in the Overworld if they have at least 1 charge
                int charges = state.get(RespawnAnchorBlock.CHARGES);
                if (charges > 0 && !client.world.getDimension().respawnAnchorWorks()) {
                    drawDangerZone(context, matrices, buffer, pos.toCenterPos(), ANCHOR_RADIUS, client.player.getEntityPos());
                }
            }
        }
    }

    private static void drawDangerZone(WorldRenderContext context, MatrixStack matrices, VertexConsumer buffer, Vec3d source, float radius, Vec3d playerPos) {
        matrices.push();

        Vec3d camera = context.gameRenderer().getCamera().getPos();
        double x = source.x - camera.x;
        double y = source.y - camera.y;
        double z = source.z - camera.z;

        matrices.translate(x, y, z);

        float r = 0, g = 1, b = 0;
        if (playerPos.distanceTo(source) <= radius) {
            r = 1.0f; g = 0.0f;
        }

        Matrix4f model = matrices.peek().getPositionMatrix();
        int segments = 64;
        float height = 2.0f; // Height of the cylinder

        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2 / segments;
            double nextAngle = (i + 1) * Math.PI * 2 / segments;

            float x1 = (float) (Math.cos(angle) * radius);
            float z1 = (float) (Math.sin(angle) * radius);
            float x2 = (float) (Math.cos(nextAngle) * radius);
            float z2 = (float) (Math.sin(nextAngle) * radius);

            // 1. Draw Bottom Ring
            buffer.vertex(model, x1, 0.05f, z1).color(r, g, b, 1.0f).normal(0, 1, 0);
            buffer.vertex(model, x2, 0.05f, z2).color(r, g, b, 1.0f).normal(0, 1, 0);

            // 2. Draw Top Ring (at 'height' meters up)
            buffer.vertex(model, x1, height, z1).color(r, g, b, 1.0f).normal(0, 1, 0);
            buffer.vertex(model, x2, height, z2).color(r, g, b, 1.0f).normal(0, 1, 0);

            // 3. Draw Vertical Pillars (every 8th segment to avoid clutter)
            if (i % 8 == 0) {
                buffer.vertex(model, x1, 0.05f, z1).color(r, g, b, 1.0f).normal(0, 1, 0);
                buffer.vertex(model, x1, height, z1).color(r, g, b, 1.0f).normal(0, 1, 0);
            }
        }

        matrices.pop();
    }
}