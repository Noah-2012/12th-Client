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

import com.noadsch12.mixininterfaces.ICameraBobbing;
import com.noadsch12.render.esp.RenderESP;
import com.noadsch12.render.util.BobbingController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Target the end of the world rendering process
    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void applyMotionBlur(RenderTickCounter tickCounter, CallbackInfo ci) {
        // Intentionally unused – motion blur is applied at the end of WorldRenderer#render
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
        if (RenderESP.INSTANCE.cancelBobbing()) {
            ci.cancel();
        }
    }
}
