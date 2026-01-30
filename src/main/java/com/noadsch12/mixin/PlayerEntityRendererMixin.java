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

import com.noadsch12.BasicGlobals;
import com.noadsch12.networking.ClientUserManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Unique
    private final Style font
            = Style.EMPTY.withFont(new StyleSpriteSource.Font(BasicGlobals.GOTHIC_FONT));

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void modifyNametagWithArial(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {

        if (state.displayName != null && !state.displayName.getString().startsWith("12C ") && isClientUser(state)) {

            Text prefix = Text.literal("12C ")
                    .setStyle(Style.EMPTY
                            .withFont(font.getFont())
                            .withColor(Formatting.WHITE));

            state.displayName = Text.empty()
                    .append(prefix)
                    .append(state.displayName);
        }
    }

    @Unique
    private boolean isClientUser(PlayerEntityRenderState state) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && state.id == client.player.getId()) {
            return true;
        }

        if (state.displayName != null) {
            String name = state.displayName.getString();
            return ClientUserManager.USERS_NAMES.contains(name);
        }

        return false;
    }
}