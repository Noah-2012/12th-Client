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

package com.noadsch12.launch;

import com.noadsch12.macro.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Launcher {

    public static void main(String[] args) {
        java.awt.Toolkit.getDefaultToolkit();

        if (args.length > 0 && "-reload".equalsIgnoreCase(args[0])) {
            doReload();
        } else if (args.length > 0) {
            openCrash(args[0]);
        }
    }

    private static void doReload() {
        try {
            System.out.println("Sending reload signal to Minecraft...");

            // create signal file
            Path signal = Path.of("reload_signal.txt");
            Files.createFile(signal);

            // wait a few seconds for Minecraft to exit
            Thread.sleep(5000);

            // now delete the old client jar safely
            Path currentJar = getCurrentJar();
            if (currentJar != null && Files.exists(currentJar)) {
                System.out.println("Deleting old client jar: " + currentJar);
                //Files.delete(currentJar);
            }

            // restart Minecraft using saved command
            List<String> launchCmd = readSavedLaunchCommand();
            if (launchCmd != null && !launchCmd.isEmpty()) {
                new ProcessBuilder(launchCmd)
                        .inheritIO()
                        .start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Path getCurrentJar() {
        try {
            return Path.of(
                    Launcher.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> readSavedLaunchCommand() {
        try {
            return Files.readAllLines(Path.of("reload_cmd.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void openCrash(String crashFile) {
        try {
            String text = Files.readString(Path.of(crashFile));
            MACROReport report = new MACROReport("Minecraft crashed", text);
            MACROWindow.show(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}