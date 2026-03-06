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

import com.noadsch12.modules.impl.combat.*;
import com.noadsch12.modules.impl.misc.*;
import com.noadsch12.modules.impl.movement.*;
import com.noadsch12.modules.impl.player.*;
import com.noadsch12.modules.impl.render.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central manager for all client modules.
 * Handles registration, retrieval, and bulk operations.
 */
public class ModuleManager {
    private static ModuleManager instance;
    private final List<Module> modules;
    private final Map<String, Module> moduleByName;
    private final Map<Category, List<Module>> modulesByCategory;

    private ModuleManager() {
        this.modules = new ArrayList<>();
        this.moduleByName = new HashMap<>();
        this.modulesByCategory = new EnumMap<>(Category.class);

        // Initialize category lists
        for (Category category : Category.values()) {
            modulesByCategory.put(category, new ArrayList<>());
        }

        // Register all modules
        registerModules();
    }

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    /**
     * Register all client modules
     */
    private void registerModules() {
        // PLAYER
        register(new AntiKnockbackModule());
        register(new AutoRefillModule());
        register(new AutoToolModule());
        register(new ChestStealerModule());
        register(new GhostHandModule());
        register(new FreecamModule());

        // MISC
        register(new JumpToFoodModule());
        register(new BetterChatModule());
        register(new ItemDisplayModule());
        register(new BetterScoreboardModule());
        register(new ShowKeystrokesModule());
        register(new StealthModeModule());

        // RENDER
        register(new HideTotemAnimModule());
        register(new HideExplosionParticlesModule());
        register(new NoDamageTiltModule());
        register(new ChestESPModule());
        register(new PlayerESPModule());
        register(new EntityCullingModule());
        register(new CompassHudModule());
        register(new FullbrightModule());
        register(new ItemRotationModule());
        register(new ProjectileDingModule());
        register(new ProjectileTrailModule());

        // COMBAT
        register(new AimbotModule());
        register(new CriticalsModule());
        register(new AutoClickerModule());
        register(new EntityEspModule());
        register(new AutoTotemModule());
        register(new AutoArmorModule());
        register(new TriggerBotModule());
        register(new AutoCobWebModule());

        // MOVEMENT
        register(new AntiWebModule());
        register(new NoSlowModule());
        register(new AntiAFKModule());
    }

    /**
     * Register a module
     */
    private void register(Module module) {
        modules.add(module);
        moduleByName.put(module.getName().toLowerCase(), module);
        modulesByCategory.get(module.getCategory()).add(module);
    }

    /**
     * Get a module by name
     */
    public Module getModule(String name) {
        return moduleByName.get(name.toLowerCase());
    }

    /**
     * Get a module by class
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) modules.stream()
                .filter(m -> m.getClass().equals(moduleClass))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all modules
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    /**
     * Get modules by category
     */
    public List<Module> getModulesByCategory(Category category) {
        return new ArrayList<>(modulesByCategory.get(category));
    }

    /**
     * Get enabled modules
     */
    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * Enable all modules in a category
     */
    public void enableCategory(Category category) {
        modulesByCategory.get(category).forEach(m -> m.setEnabled(true));
    }

    /**
     * Disable all modules in a category
     */
    public void disableCategory(Category category) {
        modulesByCategory.get(category).forEach(m -> m.setEnabled(false));
    }

    /**
     * Enable all modules
     */
    public void enableAll() {
        modules.forEach(m -> m.setEnabled(true));
    }

    /**
     * Disable all modules
     */
    public void disableAll() {
        modules.forEach(m -> m.setEnabled(false));
    }

    /**
     * Reset all modules to default state
     */
    public void resetAll() {
        modules.forEach(m -> m.setEnabled(true));
    }
}