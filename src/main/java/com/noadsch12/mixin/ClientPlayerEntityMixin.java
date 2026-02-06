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
import com.noadsch12.cheats.AutoArmor;
import com.noadsch12.cheats.AutoRefill;
import com.noadsch12.cheats.AutoTool;
import com.noadsch12.cheats.AutoTotem;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ModuleManager.getInstance().getModule("Auto Totem").isEnabled()) AutoTotem.tick();
        if (ModuleManager.getInstance().getModule("Auto Armor").isEnabled()) AutoArmor.tick();
        if (ModuleManager.getInstance().getModule("Auto Refill").isEnabled()) AutoRefill.tick();
        if (ModuleManager.getInstance().getModule("Auto Tool").isEnabled()) AutoTool.tick();
    }
}
