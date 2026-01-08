package com.noadsch12.mixin;

import com.noadsch12.render.DebugAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Inject(method = "shouldShowDebugHud", at = @At("HEAD"), cancellable = true)
    public void forceVisibility(CallbackInfoReturnable<Boolean> cir) {
        if (DebugAnimation.hasBeenActivated && DebugAnimation.shouldActuallyRender()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderStart(DrawContext context, CallbackInfo ci) {
        float p = DebugAnimation.getEasedProgress();
        // Wir verschieben es um 500 Pixel nach oben
        float offset = -500f * (1.0f - p);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, offset);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void onRenderEnd(DrawContext context, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}



