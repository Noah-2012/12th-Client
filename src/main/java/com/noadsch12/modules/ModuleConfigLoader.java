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

package com.noadsch12.modules;

import com.noadsch12.TwelfthConfig;
import com.noadsch12.modules.impl.render.TrailSettings;

/**
 * Handles loading and saving module configurations.
 * Replaces the old ClientSettingsScreen config loading code.
 */
public class ModuleConfigLoader {
    private static final ModuleManager moduleManager = ModuleManager.getInstance();

    /**
     * Load all module states from config.
     * Call this during client initialization.
     */
    public static void loadConfig() {
        // Load all modules
        for (Module module : moduleManager.getModules()) {
            String key = module.getConfigKey();

            if (TwelfthConfig.check(key)) {
                boolean enabled = (boolean) TwelfthConfig.getValue(key, "bool");
                module.setEnabled(enabled);
            } else {
                // Create default config entry (enabled by default)
                TwelfthConfig.create(key, "true");
                module.setEnabled(true);
            }
        }

        // Load trail settings
        loadTrailSettings();
    }

    /**
     * Load projectile trail settings
     */
    private static void loadTrailSettings() {
        if (TwelfthConfig.check("trail_index")) {
            int index = (int) TwelfthConfig.getValue("trail_index", "int");
            TrailSettings.setTrailIndex(index);
        } else {
            TwelfthConfig.create("trail_index", "0");
        }

        if (TwelfthConfig.check("trail_color_index")) {
            int index = (int) TwelfthConfig.getValue("trail_color_index", "int");
            TrailSettings.setTrailColorIndex(index);
        } else {
            TwelfthConfig.create("trail_color_index", "0");
        }
    }

    /**
     * Save all module states to config.
     * This is called automatically when modules are toggled,
     * but can be called manually if needed.
     */
    public static void saveConfig() {
        for (Module module : moduleManager.getModules()) {
            TwelfthConfig.setValue(module.getConfigKey(), String.valueOf(module.isEnabled()));
        }

        // Save trail settings
        TwelfthConfig.setValue("trail_index", String.valueOf(TrailSettings.getTrailIndex()));
        TwelfthConfig.setValue("trail_color_index", String.valueOf(TrailSettings.getTrailColorIndex()));
    }

    /**
     * Reset all modules to default state
     */
    public static void resetToDefaults() {
        moduleManager.resetAll();
        TrailSettings.reset();
        saveConfig();
    }
}