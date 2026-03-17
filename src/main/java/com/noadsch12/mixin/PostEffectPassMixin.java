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

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noadsch12.render.shader.PostEffectPassDuck;

import net.minecraft.client.gl.PostEffectPass;

@Mixin(PostEffectPass.class)
public class PostEffectPassMixin implements PostEffectPassDuck {

    @Shadow
    private Map<String, GpuBuffer> uniformBuffers;

    @Override
    public void _12th_Client$setUniformValue(String name, float value) {
        GpuBuffer buffer = this.uniformBuffers.get(name);
        if (buffer == null) return;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBuffer.MappedView view = encoder.mapBuffer(buffer, false, true);

        try {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putFloat(value);
        } finally {
            view.close();
        }
    }
}