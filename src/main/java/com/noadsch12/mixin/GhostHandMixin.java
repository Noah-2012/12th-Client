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

import com.noadsch12.handlers.GhostHand;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GhostHandMixin {

    @Shadow @Final
    private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void onUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        if (!GhostHand.isEnabled()) return;
        if (client.world == null || client.player == null) return;

        Entity camera = client.getCameraEntity();
        if (camera == null) return;

        double reachDistance = client.player.isCreative() ? 6.0 : 5.0;
        Vec3d cameraPos = camera.getCameraPosVec(tickDelta);
        Vec3d rotation = camera.getRotationVec(tickDelta);

        // Search through all blocks in line of sight for interactable ones
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        Vec3d step = rotation.multiply(0.1);
        Vec3d currentPos = cameraPos.add(step);

        for (int i = 0; i < (int)(reachDistance / 0.1); i++) {
            mutablePos.set(currentPos.x, currentPos.y, currentPos.z);
            BlockState state = client.world.getBlockState(mutablePos);

            // If we find a block with a block entity (chest, furnace, etc.)
            if (!state.isAir() && state.hasBlockEntity()) {
                // Override the crosshair target to this block
                client.crosshairTarget = new BlockHitResult(
                        currentPos,
                        Direction.getFacing((float)rotation.x, (float)rotation.y, (float)rotation.z),
                        mutablePos.toImmutable(),
                        false
                );
                System.out.println("GhostHand: Targeting " + state.getBlock().getName().getString() + " at " + mutablePos.toImmutable());
                return;
            }

            currentPos = currentPos.add(step);
        }
    }
}