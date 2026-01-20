package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import com.noadsch12.cheats.AutoArmor;
import com.noadsch12.cheats.AutoRefill;
import com.noadsch12.cheats.AutoTool;
import com.noadsch12.cheats.AutoTotem;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ClientSettingsScreen.AutoTotemEnabled) AutoTotem.tick();
        if (ClientSettingsScreen.AutoArmorEnabled) AutoArmor.tick();
        if (ClientSettingsScreen.AutoRefillEnabled) AutoRefill.tick();
        if (ClientSettingsScreen.AutoToolEnabled) AutoTool.tick();
    }
}
