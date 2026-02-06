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
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosionLargeParticle.class)
public class ExplosionLargeParticleMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(
            ClientWorld world, double x, double y, double z, double velocityX, SpriteProvider spriteProvider, CallbackInfo ci
    ) {
        if (ModuleManager.getInstance().getModule("Hide Explosion Particles").isEnabled()) {
            ((ExplosionLargeParticle) (Object) this).markDead();
        }
    }
}
