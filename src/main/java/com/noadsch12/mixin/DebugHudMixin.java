package com.noadsch12.mixin;

import com.noadsch12.render.ui.DebugAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
        float offset = -500f * (1.0f - p);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, offset);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void onRenderEnd(DrawContext context, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I"
            ),
            ordinal = 1
    )
    private List<String> injectRightDebugText(List<String> list) {
        if (list.stream().noneMatch(s -> s.contains("12th Client"))) {
            list.add("§b12th Client v1.1.3 by Noadsch12§r");
            list.add("The 12th Client stands under GNU 3.0 License");
        }
        return list;
    }
}



