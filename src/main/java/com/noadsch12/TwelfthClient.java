package com.noadsch12;

import com.noadsch12.discord.DiscordRichPresenceManager;
import com.noadsch12.render.EntityCulling;
import com.noadsch12.ui.ClientSettingsScreen;
import com.noadsch12.util.TwelfthCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwelfthClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");
    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 100; // Alle 5 Sekunden (100 Ticks)
    private static KeyBinding guiKeyBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("12th Client - Client initialized!");
        HotbarHelper.register();
        TwelfthCommand.register();
        EntityCulling.register();

        KeyBinding.Category clientCategory = KeyBinding.Category.create(Identifier.of("category.noadsch12.client"));

        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.client.settings_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, clientCategory));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {while (guiKeyBinding.wasPressed()) if (client.currentScreen == null) {client.setScreen(new ClientSettingsScreen(null));}});

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
