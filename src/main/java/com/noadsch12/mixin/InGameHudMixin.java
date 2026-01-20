package com.noadsch12.mixin;

import com.noadsch12.render.ui.DebugAnimation;
import com.noadsch12.render.ui.keystrokes.KeystrokesRenderer;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onUpdate(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        DebugAnimation.update();
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void cancelTotemOverlay(Text message, boolean tinted, CallbackInfo ci) {
        if (ClientSettingsScreen.HideTotemAnimEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelTotemRender(CallbackInfo ci) {
        if (ClientSettingsScreen.HideTotemAnimEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ClientSettingsScreen.ShowKeystrokeSettingsEnabled) {
            KeystrokesRenderer.render(context);
        }
    }
}