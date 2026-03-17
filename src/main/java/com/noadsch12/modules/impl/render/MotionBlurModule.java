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

package com.noadsch12.modules.impl.render;

import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.render.shader.JarShaderLoader;
import com.noadsch12.util.DeferredExecutor;

import net.irisshaders.iris.Iris;
import net.minecraft.item.Items;

public class MotionBlurModule extends Module {
    public MotionBlurModule() {
        super("Motion Blur", "Motion Blur", Category.RENDER,
                "Makes camera movements more smooth", Items.SHIELD);
    }

    @Override
    protected void onEnable() {
        DeferredExecutor.register(
        () -> Iris.getIrisConfig() != null,
        () -> {
            net.irisshaders.iris.Iris.getIrisConfig().setShadersEnabled(true);
            JarShaderLoader.loadShaderFromJar("/assets/12th-client/motion_blur.zip", "motion_blur.zip");
        }
    );
    }

    @Override
    protected void onDisable() {
        DeferredExecutor.register(
            () -> Iris.getIrisConfig() != null,
            () -> JarShaderLoader.unloadShader()
        );
    }
}
