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

import com.noadsch12.modules.impl.misc.NoBadEffectsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.joml.Vector4f;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    /**
     * applyFog() computes the fog colour Vector4f that renderWorld() passes
     * into worldRenderer.render(). When Blindness or Darkness is active the
     * renderer darkens that vector heavily.
     *
     * We inject at HEAD: if the player only has effects we're blocking, we
     * temporarily strip those effects from the local entity query by returning
     * a neutral fog colour early. To avoid actually removing server-side
     * effects we instead just short-circuit the return value with a
     * transparent/normal fog colour so the visual tint never appears.
     *
     * A cleaner approach that avoids returning a hardcoded colour is to
     * @ModifyVariable the StatusEffectInstance local variables inside applyFog,
     * but that is highly sensitive to obfuscation ordinals. HEAD + early return
     * is safer across minor version bumps.
     */
    @Inject(
            method = "applyFog",
            at = @At("HEAD"),
            cancellable = true
    )
    private void suppressDarknessFog(
            Camera camera,
            int viewDistance,
            boolean thickFog,
            RenderTickCounter tickCounter,
            float skyDarkness,
            net.minecraft.client.world.ClientWorld world,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        if (!isActive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean hasBlindness = NoBadEffectsModule.blockBlindness
                && mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
        boolean hasDarkness  = NoBadEffectsModule.blockDarkness
                && mc.player.hasStatusEffect(StatusEffects.DARKNESS);

        if (hasBlindness || hasDarkness) {
            // Return a zeroed fog vector — applyFog's normal output is the
            // sky/atmosphere colour. Returning (0,0,0,0) tells the fog pass
            // to contribute nothing, leaving the world at normal brightness.
            cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
        }
    }

    private static boolean isActive() {
        return NoBadEffectsModule.INSTANCE != null && NoBadEffectsModule.INSTANCE.isEnabled();
    }
}