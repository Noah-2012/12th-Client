/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.mixin;

import com.noadsch12.modules.ModuleManager;
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
        if (entity.age % 10 == 0 && ModuleManager.getInstance().getModule("Item Display").isEnabled()) {
            ItemLabelManager.updateItemLabel(entity, true);
        } else if (entity.age % 10 == 0 && !ModuleManager.getInstance().getModule("Item Display").isEnabled()) {
            ItemLabelManager.updateItemLabel(entity, false);
        }
    }
}