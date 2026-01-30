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

import com.noadsch12.look.ItemHexManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
        @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
        private void addHexToTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
            // 2. Create a MUTABLE copy of the original list
            List<Text> tooltip = new java.util.ArrayList<>(cir.getReturnValue());

            ItemStack stack = (ItemStack) (Object) this;
            String hex = ItemHexManager.getHexForItem(stack.getItem());

            // 3. Add your text to the copy (using index 1 to put it under the name)
            // Make sure the list isn't empty before using index 1
            if (tooltip.size() >= 1) {
                tooltip.add(1, Text.literal("§8[#" + hex + "]"));
            } else {
                tooltip.add(Text.literal("§8[#" + hex + "]"));
            }

            // 4. Override the return value with our modified list
            cir.setReturnValue(tooltip);
        }
}