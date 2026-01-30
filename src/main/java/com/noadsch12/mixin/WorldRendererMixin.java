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

package com.noadsch12.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.noadsch12.render.TrailRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import com.noadsch12.ui.BlockOutlineSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static com.noadsch12.util.world.BlockUtils.getOutlineShape;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // This shadow gives us access to the game's main vertex consumers
    @Shadow private BufferBuilderStorage bufferBuilders;

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        VertexConsumerProvider entityConsumers = this.bufferBuilders.getEntityVertexConsumers();

        TrailRenderer.render(camera, entityConsumers, positionMatrix);
    }

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, double x, double y, double z, OutlineRenderState state, int i, CallbackInfo ci) {
        if (BlockOutlineSettings.enabled) {
            ci.cancel(); // Prevent vanilla outline

            BlockPos pos = state.pos();
            VoxelShape shape = getOutlineShape(pos); // Basic context-less shape

            Color color = BlockOutlineSettings.getOutlineColor();
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = BlockOutlineSettings.a;

            // Using your preferred matrix naming
            matrices.push();

            // Translate to the block's world position relative to camera
            matrices.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

            Matrix4f posMatrix = matrices.peek().getPositionMatrix();

            // Draw the lines for the shape
            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                vertexConsumer.vertex(posMatrix, (float)x1, (float)y1, (float)z1)
                        .color(red, green, blue, alpha)
                        .normal(matrices.peek(), (float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1));

                vertexConsumer.vertex(posMatrix, (float)x2, (float)y2, (float)z2)
                        .color(red, green, blue, alpha)
                        .normal(matrices.peek(), (float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1));
            });

            matrices.pop();
        }
    }
}