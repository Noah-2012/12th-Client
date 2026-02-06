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

import blue.endless.jankson.annotation.Nullable;
import com.noadsch12.cheats.PlayerAimbotHandler;
import com.noadsch12.look.ItemHexManager;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    public Screen currentScreen;
    @Shadow protected abstract boolean doAttack();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player != null && ModuleManager.getInstance().getModule("Aimbot").isEnabled()) {
            PlayerAimbotHandler.updateAimbot(client);
        }

        if (ModuleManager.getInstance().getModule("Auto Clicker").isEnabled() && this.currentScreen == null) {
            this.doAttack();
        }

        ItemHexManager.tick();
    }
}