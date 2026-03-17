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

package com.noadsch12.render.shader;

import net.irisshaders.iris.Iris;
import net.minecraft.client.MinecraftClient;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JarShaderLoader {

    public static void loadShaderFromJar(String resourcePath, String fileName) {
        try {
            if (Iris.getIrisConfig() == null) return;

            Path shaderpackPath = MinecraftClient.getInstance().runDirectory.toPath()
                    .resolve("shaderpacks").resolve(fileName);

            try (InputStream is = JarShaderLoader.class.getResourceAsStream(resourcePath)) {
                if (is == null) return;
                Files.createDirectories(shaderpackPath.getParent());
                Files.copy(is, shaderpackPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Iris.getIrisConfig().setShaderPackName(fileName);
            Iris.getIrisConfig().save();

            Iris.reload();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unloadShader() {
        try {
            if (Iris.getIrisConfig() == null) return;

            Iris.getIrisConfig().setShaderPackName("");
            Iris.getIrisConfig().save();

            Iris.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
