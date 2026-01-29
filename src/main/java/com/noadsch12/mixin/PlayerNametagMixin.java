package com.noadsch12.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerNametagMixin {

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void showLocalPlayerLabel(PlayerLikeEntity playerLikeEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if the entity being rendered is the local player
        if (playerLikeEntity == client.player) {
            // Only show if we are in third person (F5)
            if (!client.options.getPerspective().isFirstPerson()) {
                cir.setReturnValue(true);
            }
        }
    }
}