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

import com.noadsch12.discord.DiscordRichPresenceManager;
import com.noadsch12.look.ObjModel;
import com.noadsch12.modules.ModuleConfigLoader;
import com.noadsch12.networking.ClientStatusPayload;
import com.noadsch12.networking.ClientUserManager;
import com.noadsch12.render.fx.ExplosiveScanner;
import com.noadsch12.render.esp.RenderESP;
import com.noadsch12.render.entity.EntityCulling;
import com.noadsch12.render.ui.CompassHud;
import com.noadsch12.render.ui.ObjWireframeHud;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import com.noadsch12.util.DeferredExecutor;
import com.noadsch12.util.world.HotbarHelper;
import com.noadsch12.util.Stealth;
import com.noadsch12.util.TwelfthCommand;
import com.noadsch12.macro.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class TwelfthClient implements ClientModInitializer {

    // -------------------------------------------------------------------------
    // Constants & Static Fields
    // -------------------------------------------------------------------------

    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");
    public static final String MOD_ID  = "12th-client";
    public static final String NAME    = "12th Client";

    private static final int DISCORD_UPDATE_INTERVAL = 100; // Every 5 seconds (100 ticks)

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    public static boolean isMenuAlreadyOpen = false;
    public static ObjModel myLoadedModel;

    private float discordTickCounter = 0;
    private static KeyBinding guiKeyBinding;

    // =========================================================================
    // ClientModInitializer Entry Point
    // =========================================================================

    @Override
    public void onInitializeClient() {
            LOGGER.info("{} - Client initialized!", NAME);

            setupCrashHandler();
            setupHotReload();

            ModuleConfigLoader.loadConfig();

            registerCoreFeatures();
            registerNetworking();
            registerKeybindings();
            registerTickHandlers();
            registerHudRendering();
            registerLifecycleEvents();

    }

    // =========================================================================
    // Initialisation Steps
    // =========================================================================

    /** Sets a global uncaught exception handler that forwards crashes to MACRO. */
    private void setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                MACRO.handleCrash(throwable));
    }

    /**
     * Hot-reload system: saves the current launch command so an external script
     * can restart Minecraft. A watcher thread polls for a signal file and
     * triggers a clean shutdown when found.

     * !! DO NOT EDIT THIS UNLESS YOU KNOW WHAT YOU ARE DOING !!
     */
    private void setupHotReload() {
        // Save launch command for the hot-reload launcher script
        ProcessHandle.current().info().commandLine().ifPresent(cmd -> {
            try (PrintWriter writer = new PrintWriter(Path.of("reload_cmd.txt").toFile())) {
                writer.println(cmd);
                LOGGER.info("Saved launch command: {}", cmd);
            } catch (Exception e) {
                LOGGER.error("Failed to save launch command", e);
            }
        });

        createReloadWatcherThread().start();
    }

    /**
     * Creates the daemon thread that watches for a reload signal file
     * and triggers a clean Minecraft shutdown when found.
     */
    private Thread createReloadWatcherThread() {
        Thread reloadWatcher = new Thread(() -> {
            Path signal = Path.of("reload_signal.txt");

            while (true) {
                try {
                    if (Files.exists(signal)) {
                        LOGGER.info("Reload signal received — exiting Minecraft...");
                        Files.delete(signal);
                        MinecraftClient.getInstance().execute(MinecraftClient.getInstance()::scheduleStop);
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error in ReloadWatcher thread", e);
                }
            }
        }, "ReloadWatcher");

        reloadWatcher.setDaemon(true);
        return reloadWatcher;
    }

    /** Registers helpers and commands that don't fit a narrower category. */
    private void registerCoreFeatures() {
        HotbarHelper.register();
        TwelfthCommand.register();
        EntityCulling.register();
        RenderESP.init();
        ExplosiveScanner.init();

        CompassHud.addWaypoint("Test", 0.0, 60.0, 0.0);
    }

    /** Registers all custom network packet receivers. */
    private void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
                ClientStatusPayload.ID,
                (payload, context) ->
                        context.client().execute(() ->
                                ClientUserManager.USERS.add(payload.playerUuid()))
        );
    }

    /** Registers all client-side keybindings. */
    private void registerKeybindings() {
        KeyBinding.Category clientCategory =
                KeyBinding.Category.create(Identifier.of("category.noadsch12.client"));

        // Right-Shift → open settings GUI (toggle)
        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.client.settings_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                clientCategory
        ));

        // Grave Accent (`) → hold to keep settings GUI open; release to close
        ClientTickEvents.END_CLIENT_TICK.register(this::tickGraveAccentMenu);

        // Right-Shift pressed event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClientSettingsScreen(null));
                }
            }
        });
    }

    /** Registers all END_CLIENT_TICK listeners. */
    private void registerTickHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> DeferredExecutor.tick());
        ClientTickEvents.END_CLIENT_TICK.register(Stealth::onTick);
        ClientTickEvents.END_CLIENT_TICK.register(this::tickDiscord);
    }

    /** Registers the HUD elements using the v1 hud package API. */
    private void registerHudRendering() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                TwelfthClient.identifier("hud"),
                (context, tickCounter) -> {
                    CompassHud.render(context);
                    ensureModelLoaded();
                    ObjWireframeHud.render(context, myLoadedModel, 39, 38, 42.6f);
                }
        );
    }

    /** Registers CLIENT_STARTED and CLIENT_STOPPING lifecycle events. */
    private void registerLifecycleEvents() {
        DiscordRichPresenceManager.init();

        ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                DiscordRichPresenceManager.updatePresence("Client started", "In Main Menu"));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client ->
                DiscordRichPresenceManager.shutdown());
    }

    // =========================================================================
    // Tick Handlers
    // =========================================================================

    /**
     * Handles the hold-to-open behavior for the Grave Accent key:
     * – Opens the settings screen while the key is held.
     * – Closes the settings screen when the key is released.
     */
    private void tickGraveAccentMenu(MinecraftClient client) {
        if (client.getWindow() == null || client.player == null) return;

        boolean isKeyDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_GRAVE_ACCENT);

        if (isKeyDown) {
            if (!isMenuAlreadyOpen && client.currentScreen == null) {
                client.setScreen(new ClientSettingsScreen(null));
                isMenuAlreadyOpen = true;
            }
        } else {
            if (isMenuAlreadyOpen) {
                if (client.currentScreen instanceof ClientSettingsScreen) {
                    client.setScreen(null);
                }
                isMenuAlreadyOpen = false;
            }
        }

        // Safety: user pressed ESC manually — reset flag
        if (isMenuAlreadyOpen && client.currentScreen == null) {
            isMenuAlreadyOpen = false;
        }
    }

    /** Runs Discord callbacks every tick and updates presence every 5 seconds. */
    private void tickDiscord(MinecraftClient client) {
        DiscordRichPresenceManager.tick();

        if (++discordTickCounter >= DISCORD_UPDATE_INTERVAL) {
            discordTickCounter = 0;
            DiscordRichPresenceManager.updateForInGame();
        }
    }

    // =========================================================================
    // Utility / Helpers
    // =========================================================================

    /** Lazily loads the OBJ wireframe model from the mod's resource pack. */
    public static void ensureModelLoaded() {
        if (myLoadedModel != null) return;

        Identifier modelId = Identifier.of(MOD_ID, "models/misc/low_poly_sphere.obj");

        MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(modelId)
                .ifPresent(resource -> {
                    try (InputStream stream = resource.getInputStream()) {
                        myLoadedModel = new ObjModel(stream);
                    } catch (IOException e) {
                        LOGGER.error("Failed to load OBJ model: {}", modelId, e);
                    }
                });
    }

    /** Shorthand for creating a mod-namespaced {@link Identifier}. */
    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}