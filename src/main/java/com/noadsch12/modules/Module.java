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
import com.noadsch12.ui.GLWindow;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

/**
 * Base class for all client modules/features.
 * Each module represents a toggleable feature with configuration.
 */
public abstract class Module {
    private final String name;
    private final String displayName;
    private final String configKey;
    private final Category category;
    private final String tooltip;
    private final Item iconItem;
    private boolean enabled;
    private GLWindow settingsWindow;

    public Module(String name, String displayName, Category category, String tooltip, Item iconItem) {
        this.name = name;
        this.displayName = displayName;
        this.configKey = name.toLowerCase().replace(" ", "_") + "_enabled";
        this.category = category;
        this.tooltip = tooltip;
        this.iconItem = iconItem;
        this.enabled = true; // Default enabled
    }

    /**
     * Called when the module is toggled on
     */
    protected void onEnable() {
        // Override in subclasses if needed
    }

    /**
     * Called when the module is toggled off
     */
    protected void onDisable() {
        // Override in subclasses if needed
    }

    /*
     * Override for a optional Settings Screen
     */
    protected GLWindow createSettingsWindow() {
        return null; // default: no settings
    }

    /**
     * Toggle the module on/off
     */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Set the enabled state
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        TwelfthConfig.setValue(configKey, String.valueOf(enabled));

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /*
     * Public getter for the optional Settings Screen
     */
    public GLWindow getSettingsWindow() {
        if (settingsWindow == null) {
            settingsWindow = createSettingsWindow();
        }
        return settingsWindow;
    }

    /**
     * Get the button label with status color
     */
    public Text getButtonLabel() {
        String status = enabled ? "§aON" : "§cOFF";
        return Text.literal(displayName + ": " + status);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Category getCategory() {
        return category;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Item getIconItem() {
        return iconItem;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
