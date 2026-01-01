package com.noadsch12.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileDing {

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void onArrowHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

        if (entityHitResult.getEntity() instanceof net.minecraft.entity.LivingEntity target) {

            boolean killed = !target.isAlive();

            if (arrow.getOwner() instanceof ServerPlayerEntity player) {
                player.playSoundToPlayer(
                        SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
                        SoundCategory.MASTER,
                        10.0f,
                        killed ? 0.6f : 6.0f
                );
            }
        }
    }
}
