package com.noadsch12.mixin;

import com.noadsch12.networking.ClientUserManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityUpdateMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void captureUuid(T entity, S state, float tickProgress, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && state instanceof PlayerEntityRenderState) {
            // If the player is a client user, we store their NAME in a set
            // so the other Mixin can find it via state.displayName
            if (ClientUserManager.USERS.contains(player.getUuid())) {
                ClientUserManager.USERS_NAMES.add(player.getName().getString());
            }
        }
    }
}