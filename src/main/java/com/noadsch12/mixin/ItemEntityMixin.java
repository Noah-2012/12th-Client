package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.entity.ItemEntity;
import com.noadsch12.render.items.ItemLabelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // Cast 'this' to ItemEntity
        ItemEntity entity = (ItemEntity) (Object) this;

        // Only update every 20 ticks (1 second) to save performance,
        // or remove the check for real-time updates.
        if (entity.age % 10 == 0 && ClientSettingsScreen.ItemDisplayEnabled) {
            ItemLabelManager.updateItemLabel(entity, true);
        } else if (entity.age % 10 == 0 && !ClientSettingsScreen.ItemDisplayEnabled) {
            ItemLabelManager.updateItemLabel(entity, false);
        }
    }
}