package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NoItemSlowdownMixin {

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    private void removeUseItemSlowdown(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only apply to players
        if (entity instanceof PlayerEntity player && ClientSettingsScreen.NoSlowEnabled) {
            // isUsingItem() is true when eating, drinking, or using a bow/shield
            if (player.isUsingItem()) {
                // By default, Minecraft returns 0.2f here.
                // We reset it to the player's normal base movement speed.
                cir.setReturnValue(player.getMovementSpeed());
            }
        }
    }
}