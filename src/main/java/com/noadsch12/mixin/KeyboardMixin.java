package com.noadsch12.mixin;

import com.noadsch12.render.DebugAnimation;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (input.key() == 292 && action == 1) { // F3 Pressed
            // If it's the FIRST time ever pressing F3, don't redirect yet
            if (!DebugAnimation.hasBeenActivated) {
                DebugAnimation.hasBeenActivated = true;
                DebugAnimation.isF3Visible = true;
                // Allow vanilla to handle it this one time to spawn the HUD
                return;
            }

            // After the first time, we take full control
            DebugAnimation.isF3Visible = !DebugAnimation.isF3Visible;
            ci.cancel();
        }
    }
}

