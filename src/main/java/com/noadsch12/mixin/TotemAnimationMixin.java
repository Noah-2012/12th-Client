package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class TotemAnimationMixin {
    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void cancelTotemAnimation(ItemStack floatingItem, CallbackInfo ci) {
        if (ClientSettingsScreen.HideTotemAnimEnabled && floatingItem.isOf(Items.TOTEM_OF_UNDYING)) {
            ci.cancel();
        }
    }
}