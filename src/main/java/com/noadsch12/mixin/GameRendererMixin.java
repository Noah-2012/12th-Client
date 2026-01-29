package com.noadsch12.mixin;

import com.noadsch12.render.MotionBlurManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Target the end of the world rendering process
    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void applyMotionBlur(RenderTickCounter tickCounter, CallbackInfo ci) {
        //MotionBlurManager.render();
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    private void onResized(int width, int height, CallbackInfo ci) {
        //MotionBlurManager.onResize();
    }
}