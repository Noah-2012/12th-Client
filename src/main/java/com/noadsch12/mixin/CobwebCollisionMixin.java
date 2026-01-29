package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobwebBlock.class)
public class CobwebCollisionMixin {

    /**
     * Prevents the cobweb from applying the "slowdown" effect to the player.
     */
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void stopWebSlowdown(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl, CallbackInfo ci) {
        // Use your utility logic or check for player
        if (entity instanceof PlayerEntity && ClientSettingsScreen.AntiWebEnabled) {
            // Cancel the method to skip the code that sets velocity to (0.25, 0.05, 0.25)
            ci.cancel();
        }
    }
}