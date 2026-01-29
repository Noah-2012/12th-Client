package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientPlayerEntity.class)
public class NoSlowMixin {
    @ModifyConstant(method = "applyMovementSpeedFactors", constant = @Constant(floatValue = 0.2F))
    private float removeSlowdown(float constant) {
        // If the feature is enabled, return 1.0f (no slowdown) instead of 0.2f
        if (!ClientSettingsScreen.NoSlowEnabled) return constant;
        return 1.0f;
    }
}