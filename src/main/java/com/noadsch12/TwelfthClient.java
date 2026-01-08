package com.noadsch12;

import com.noadsch12.discord.DiscordRichPresenceManager;
import com.noadsch12.render.EntityCulling;
import com.noadsch12.ui.ClientSettingsScreen;
import com.noadsch12.ui.ModConfigScreen;
import com.noadsch12.util.TwelfthCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TwelfthClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");
    private float tickCounter = 1.0f;
    private static final int UPDATE_INTERVAL = 100; // Alle 5 Sekunden (100 Ticks)
    private static KeyBinding guiKeyBinding;
    public static boolean fastguiopen = false;
    private boolean isMenuAlreadyOpen = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("12th Client - Client initialized!");
        HotbarHelper.register();
        TwelfthCommand.register();
        EntityCulling.register();

        KeyBinding.Category clientCategory = KeyBinding.Category.create(Identifier.of("category.noadsch12.client"));

        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.client.settings_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, clientCategory));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {while (guiKeyBinding.wasPressed()) if (client.currentScreen == null) {client.setScreen(new ClientSettingsScreen(null));}});

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null || client.player == null) return;

            boolean isKeyDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_GRAVE_ACCENT);

            if (isKeyDown) {
                // Only open if we haven't ALREADY sent the "open" command
                if (!isMenuAlreadyOpen && client.currentScreen == null) {
                    client.setScreen(new ClientSettingsScreen(null));
                    isMenuAlreadyOpen = true;
                    System.out.println("Menu opened!"); // Debug check in console
                }
            } else {
                // Only close if we are currently in our "Open" state
                if (isMenuAlreadyOpen) {
                    if (client.currentScreen instanceof ClientSettingsScreen) {
                        client.setScreen(null);
                    }
                    isMenuAlreadyOpen = false;
                    System.out.println("Menu closed!"); // Debug check in console
                }
            }

            // Safety: If the user presses ESC manually, reset our state variable
            if (isMenuAlreadyOpen && client.currentScreen == null) {
                isMenuAlreadyOpen = false;
            }
        });

        // Discord RPC initialisieren
        DiscordRichPresenceManager.init();

        // Tick Handler für Updates
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Discord Callbacks jedes Tick verarbeiten (wichtig!)
            DiscordRichPresenceManager.tick();

            // Presence nur alle X Ticks aktualisieren
            tickCounter++;
            if (tickCounter >= UPDATE_INTERVAL) {
                tickCounter = 0;
                DiscordRichPresenceManager.updateForInGame();
            }
        });

        // Shutdown Hook für sauberes Beenden
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            DiscordRichPresenceManager.shutdown();
        });

        // Update bei Client-Start
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DiscordRichPresenceManager.updatePresence("Client gestartet", "Im Hauptmenü");
        });
    }
}
