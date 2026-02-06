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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NoItemSlowdownMixin {

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    private void removeUseItemSlowdown(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only apply to players
        if (entity instanceof PlayerEntity player && ModuleManager.getInstance().getModule("No Slow").isEnabled()) {
            // isUsingItem() is true when eating, drinking, or using a bow/shield
            if (player.isUsingItem()) {
                // By default, Minecraft returns 0.2f here.
                // We reset it to the player's normal base movement speed.
                cir.setReturnValue(player.getMovementSpeed());
            }
        }
    }
}