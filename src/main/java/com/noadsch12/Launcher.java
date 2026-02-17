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

package com.noadsch12;

import com.noadsch12.render.MotionBlurOverlay;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Launcher {

    public static void main(String[] args) {

        // Force AWT init early (VERY important)
        java.awt.Toolkit.getDefaultToolkit();

        if (args.length > 0 && args[0] != null) {
            openCrash(args[0]);
        } else {
            // Motion Blur Overlay für Minecraft
            SwingUtilities.invokeLater(() -> {
                try {
                    MotionBlurOverlay overlay = new MotionBlurOverlay();
                    overlay.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void openCrash(String crashFile) {

        try {

            String stacktrace = Files.readString(Path.of(crashFile));

            // Build your CrashReport object
            MACROReport report = new MACROReport(
                    "Minecraft crashed",
                    stacktrace
            );

            // Open your existing UI
            MACROWindow.show(report);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
