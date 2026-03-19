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

import com.noadsch12.modules.impl.movement.SafeWalkModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class SafeWalkMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void applySafeWalk(CallbackInfo ci) {
        if (!isActive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        if (!player.isOnGround()) return;
        if (player.isSneaking())  return;

        // Cast to our accessor to reach the Input fields
        InputAccessor input = (InputAccessor) this;
        Vec2f vec = input.getMovementVector();

        float fwd  = vec.y;  // +1 = forward, -1 = backward
        float side = vec.x;  // +1 = strafe right, -1 = strafe left

        if (fwd == 0f && side == 0f) return;

        // ── Yaw → world-space direction vectors ───────────────────────────────
        float yaw    = player.getYaw() * ((float) Math.PI / 180f);
        float sinYaw = MathHelper.sin(yaw);
        float cosYaw = MathHelper.cos(yaw);

        float fwdDX  = -sinYaw;

        final float STEP = 0.65f;
        Box bbox = player.getBoundingBox();

        float newFwd  = fwd;
        float newSide = side;

        // ── Forward / backward axis ───────────────────────────────────────────
        if (fwd != 0f) {
            float dx = fwdDX * fwd * STEP;
            float dz = cosYaw * fwd * STEP;
            if (!hasSolidGroundBelow(mc.world, bbox.offset(dx, 0, dz))) {
                newFwd = 0f;
            }
        }

        // ── Strafe axis ───────────────────────────────────────────────────────
        if (side != 0f) {
            float dx = cosYaw * side * STEP;
            float dz = sinYaw * side * STEP;
            if (!hasSolidGroundBelow(mc.world, bbox.offset(dx, 0, dz))) {
                newSide = 0f;
            }
        }

        // ── Diagonal safety net ───────────────────────────────────────────────
        if (newFwd != 0f && newSide != 0f) {
            float dx = fwdDX * newFwd * STEP + cosYaw * newSide * STEP;
            float dz = cosYaw * newFwd * STEP + sinYaw * newSide * STEP;
            if (!hasSolidGroundBelow(mc.world, bbox.offset(dx, 0, dz))) {
                newFwd  = 0f;
                newSide = 0f;
            }
        }

        // ── Write back if anything changed ────────────────────────────────────
        if (newFwd != fwd || newSide != side) {
            input.setMovementVector((new Vec2f(newSide, newFwd)).normalize());
        }
    }

    /**
     * Returns true if at least one solid collision shape exists directly
     * beneath the XZ footprint of the given bounding box.
     */
    @Unique
    private static boolean hasSolidGroundBelow(World world, Box box) {
        int checkY = MathHelper.floor(box.minY) - 1;
        int minX   = MathHelper.floor(box.minX);
        int maxX   = MathHelper.floor(box.maxX - 1e-7);
        int minZ   = MathHelper.floor(box.minZ);
        int maxZ   = MathHelper.floor(box.maxZ - 1e-7);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos bp = new BlockPos(x, checkY, z);
                if (!world.getBlockState(bp)
                        .getCollisionShape(world, bp).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static boolean isActive() {
        return SafeWalkModule.INSTANCE != null
                && SafeWalkModule.INSTANCE.isEnabled();
    }
}