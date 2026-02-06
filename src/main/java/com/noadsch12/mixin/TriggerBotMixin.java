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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Replace with the actual package where your settings are stored
import com.noadsch12.ui.screens.ClientSettingsScreen;

@Mixin(MinecraftClient.class)
public abstract class TriggerBotMixin {

    @Shadow @Nullable public HitResult crosshairTarget;
    @Shadow @Nullable public Screen currentScreen;
    @Shadow protected abstract boolean doAttack();

    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Unique
    private static MinecraftClient client;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // 1. Check if the feature is enabled
        // 2. Ensure no menu is open
        // 3. Ensure the crosshair is actually over an Entity
        if (ModuleManager.getInstance().getModule("Trigger Bot").isEnabled()
                && this.currentScreen == null
                && this.crosshairTarget != null
                && this.crosshairTarget.getType() == HitResult.Type.ENTITY) {

            Entity target = ((EntityHitResult)this.crosshairTarget).getEntity();

            // We can further verify it's an EntityHitResult to be safe
            if (this.crosshairTarget instanceof EntityHitResult && target != this.player) {
                this.doAttack();
            }
        }
    }
}