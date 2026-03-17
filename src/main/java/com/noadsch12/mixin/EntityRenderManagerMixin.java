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

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.render.entity.EntityESP;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class EntityRenderManagerMixin {

    @Shadow @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(ObjectAllocator allocator, RenderTickCounter tickCounter,
                             boolean renderBlockOutline, Camera camera,
                             Matrix4f positionMatrix, Matrix4f viewMatrix,
                             Matrix4f projectionMatrix, GpuBufferSlice fogBuffer,
                             Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (ModuleManager.getInstance().getModule("Entity ESP").isEnabled()) {
            EntityESP.render(camera, bufferBuilders.getEntityVertexConsumers(), positionMatrix);
        }
    }
}
