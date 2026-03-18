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
import com.noadsch12.modules.impl.render.ItemRotationModule;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private static void onRenderItem(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List<BakedQuad> quads, RenderLayer layer, ItemRenderState.Glint glint, CallbackInfo ci) {
        if (!ModuleManager.getInstance().getModule("Item Rotation").isEnabled()) return;
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {

            applyCustomTransformations(matrices, displayContext);
        }
    }

    @Unique
    private static void applyCustomTransformations(MatrixStack matrices, ItemDisplayContext displayContext) {
        matrices.translate(ItemRotationModule.xOffset, ItemRotationModule.yOffset, ItemRotationModule.zOffset);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ItemRotationModule.rotation_axis_y));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ItemRotationModule.rotation_axis_x));
    }
}
