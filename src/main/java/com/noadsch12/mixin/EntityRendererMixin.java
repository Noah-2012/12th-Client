package com.noadsch12.mixin;

import com.noadsch12.render.EntityCulling;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    /**
     * Injected into the `shouldRender` method of `EntityRenderer`
     * This method determines whether an entity should be rendered at all.
     */
    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyCulling(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!EntityCulling.shouldRender(entity, frustum)) {
            cir.setReturnValue(false);
        }
    }
}