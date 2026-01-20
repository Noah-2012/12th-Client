package com.noadsch12.mixin;

import com.noadsch12.render.ui.DebugAnimation;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (input.key() == 292 && action == 1) {
            if (!DebugAnimation.hasBeenActivated) {
                DebugAnimation.hasBeenActivated = true;
                DebugAnimation.isF3Visible = true;
                return;
            }

            DebugAnimation.isF3Visible = !DebugAnimation.isF3Visible;
            ci.cancel();
        }
    }
}

