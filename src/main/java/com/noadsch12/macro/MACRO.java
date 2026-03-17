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

package com.noadsch12.macro;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MACRO {

    public static void handleCrash(Throwable t) {

        try {
            // 1. Pfad zur aktuellen Java-Executable (unabhängig vom Minecraft-Exit)
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            // 2. Deine Mod-JAR dynamisch finden (via Fabric Loader API)
            Path modPath = FabricLoader.getInstance().getModContainer("twelfth-client")
                    .map(container -> container.getOrigin().getPaths().getFirst().toAbsolutePath())
                    .orElseThrow();

            // 3. Temporäre Crash-Datei erstellen
            Path tempFilePath = Files.createTempFile(FabricLoader.getInstance().getGameDir(), "12th-client-crash-", ".txt");

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempFilePath))) {
                writer.println("12th Client Crash Report");
                writer.println("=========================");
                t.printStackTrace(writer); // Schreibt den kompletten Stacktrace
            }

            // 4. Den neuen, völlig eigenständigen Prozess starten
            new ProcessBuilder(
                    javaBin,
                    "-jar",
                    modPath.toString(),
                    tempFilePath.toAbsolutePath().toString()
            ).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
