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
import com.noadsch12.modules.impl.misc.BetterChatModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenAnimationMixin {

    @Unique private long openTime;
    @Unique private boolean pushed = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        if (!ModuleManager.getInstance().getModule("Better Chat").isEnabled()) return;
        if (!BetterChatModule.showAnimations) return;

        this.openTime = System.currentTimeMillis();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void startAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ModuleManager.getInstance().getModule("Better Chat").isEnabled()) return;
        if (!BetterChatModule.showAnimations) return;

        context.getMatrices().pushMatrix();
        pushed = true;

        long elapsed = System.currentTimeMillis() - openTime;
        long duration = 300;

        if (elapsed < duration) {
            float progress = (float) elapsed / duration;
            float ease = 1.0f - (float) Math.pow(1.0 - progress, 4);
            float yOffset = 20.0f * (1.0f - ease);
            context.getMatrices().translate(0.0f, yOffset);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void endAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BetterChatModule.showAnimations) return;

        if (pushed) {
            context.getMatrices().popMatrix();
            pushed = false;
        }
    }
}
