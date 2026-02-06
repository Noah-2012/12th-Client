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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import com.noadsch12.render.SmallAsciiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private ResourceReload reload;

    @Unique
    private static final String ASCII_RAW = """
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /
    /__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/_
    \\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\  /\\\s
    _\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\/__\\
    """;

    @Unique
    private static final String[] MASSIVE_ASCII = ASCII_RAW.split("\n");

    @Unique
    private static final long CUSTOM_FADE_DURATION = 1320; // 1.32 seconds

    /**
     * Changes the background color to Dark Grey.
     * Targets the color parameter of the fill method.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"), index = 4)
    private int changeColor(int color) {
        return 0xFF2D2D2D; // Dark Grey
    }

    /**
     * Injects at the very start of the render method.
     * Since we call our custom renderer here, it will be drawn FIRST
     * (behind the logo/loading bar which are drawn later in the method).
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void renderAsciiBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Ensure the background color is drawn first so ASCII isn't covered by the default red
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0xFF2D2D2D);

        // Now draw the ASCII using your custom renderer
        SmallAsciiRenderer.drawAsciiArt(
                context,
                this.client.textRenderer,
                MASSIVE_ASCII,
                0f, 0f,
                6.0f,
                0xFF666667
        );
    }
}