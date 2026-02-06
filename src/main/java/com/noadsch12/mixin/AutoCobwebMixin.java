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

import com.noadsch12.modules.ModuleManager;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class AutoCobwebMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Check your custom setting toggle
        if (!ModuleManager.getInstance().getModule("Anti Web").isEnabled()) return;

        // Ensure player is holding cobwebs (optional, remove if you want it to auto-switch)
        if (player.getStackInHand(Hand.MAIN_HAND).getItem() != Items.COBWEB) return;

        // Logic: Check position at feet or 1 block in front
        BlockPos pos = player.getBlockPos();

        if (shouldPlaceCobweb(player, pos)) {
            placeCobweb(player, pos);
        } else {
            BlockPos forwardPos = pos.offset(player.getHorizontalFacing());
            if (shouldPlaceCobweb(player, forwardPos)) {
                placeCobweb(player, forwardPos);
            }
        }
    }

    private boolean shouldPlaceCobweb(ClientPlayerEntity player, BlockPos pos) {
        // Only place if the block is air/replaceable and there is a target nearby
        return player.getEntityWorld().getBlockState(pos).isReplaceable();
    }

    private void placeCobweb(ClientPlayerEntity player, BlockPos pos) {
        var client = net.minecraft.client.MinecraftClient.getInstance();

        // Create a fake hit result to simulate the click
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                Direction.UP,
                pos,
                false
        );

        // Interact with the world
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
    }
}