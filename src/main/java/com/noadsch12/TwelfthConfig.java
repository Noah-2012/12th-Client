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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class TwelfthConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("twelfth_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- Internal Helpers ---

    private static JsonObject load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            return new JsonObject();
        }
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            return new JsonObject();
        }
    }

    private static void save(JsonObject json) {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Public Methods ---

    /**
     * Returns true if the entry exists in the config file.
     */
    public static boolean check(String name) {
        return load().has(name);
    }

    /**
     * Creates an entry if it doesn't already exist.
     */
    public static void create(String name, @NotNull String value) {
        JsonObject config = load();
        if (!config.has(name)) {
            config.addProperty(name, value);
            save(config);
        }
    }

    /**
     * Edits an existing entry or creates it if missing.
     */
    public static void setValue(String name, @NotNull String value) {
        JsonObject config = load();
        config.addProperty(name, value);
        save(config);
    }

    /**
     * Returns the value in the specified format (string, int, bool, float).
     */
    public static Object getValue(String name, @NotNull String format) {
        JsonObject config = load();
        if (!config.has(name)) return null;

        String val = config.get(name).getAsString();

        return switch (format.toLowerCase()) {
            case "int" -> Integer.parseInt(val);
            case "bool", "boolean" -> Boolean.parseBoolean(val);
            case "float" -> Float.parseFloat(val);
            default -> val;
        };
    }

    /**
     * Overrides a complete entry (functionally updates the key).
     */
    public static void override(String name, @NotNull String value) {
        setValue(name, value);
    }

    /**
     * Deletes an entry from the config file.
     */
    public static void delete(String name) {
        JsonObject config = load();
        if (config.has(name)) {
            config.remove(name);
            save(config);
        }
    }
}