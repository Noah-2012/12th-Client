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

import com.noadsch12.MACRO;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class CrashCatcherPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {

        Thread.UncaughtExceptionHandler original =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {

            // Your handler FIRST
            try {
                MACRO.handleCrash(throwable);
            } catch (Throwable ignored) {}

            // Then Minecraft/Fabric handler
            if (original != null) {
                original.uncaughtException(thread, throwable);
            }
        });
    }
}
