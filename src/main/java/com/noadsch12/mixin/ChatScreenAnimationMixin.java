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

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenAnimationMixin {

    @Unique
    private long openTime;

    // Capture the time when the chat screen is initialized (opened)
    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        if (ClientSettingsScreen.BetterChatEnabled) {
            this.openTime = System.currentTimeMillis();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void startAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ClientSettingsScreen.BetterChatEnabled) return;

        // Ensure we push the matrix as requested
        context.getMatrices().pushMatrix();

        long elapsed = System.currentTimeMillis() - openTime;
        long duration = 300; // Matching your message animation duration

        if (elapsed < duration) {
            float progress = (float) elapsed / duration;
            // Using the same Quartic Ease-Out as your ChatHud animation
            float ease = 1.0f - (float) Math.pow(1.0 - progress, 4);

            // Slide up from 20 pixels below (adjust the 20.0f for more/less slide)
            float yOffset = 20.0f * (1.0f - ease);

            // Per your instructions: translate(x, y)
            context.getMatrices().translate(0.0f, yOffset);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void endAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ClientSettingsScreen.BetterChatEnabled) {
            // Match the push with a popMatrix
            context.getMatrices().popMatrix();
        }
    }
}