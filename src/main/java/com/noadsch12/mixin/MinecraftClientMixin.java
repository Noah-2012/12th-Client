package com.noadsch12.mixin;

import com.noadsch12.cheats.PlayerAimbotHandler;
import com.noadsch12.look.ItemHexManager;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player != null && ClientSettingsScreen.AimbotEnabled) {
            PlayerAimbotHandler.updateAimbot(client);
        }

        ItemHexManager.tick();
    }
}