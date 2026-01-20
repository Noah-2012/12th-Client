package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.particle.ExplosionSmokeParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosionSmokeParticle.class)
public class ExplosionSmokeParticleMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(
            ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci
    ) {
        if (ClientSettingsScreen.HideExplosionParticlesEnabled) {
            ((ExplosionSmokeParticle) (Object) this).markDead();
        }
    }
}
