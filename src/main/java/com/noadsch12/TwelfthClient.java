package com.noadsch12;

import com.noadsch12.discord.DiscordRichPresenceManager;
import com.noadsch12.render.entity.EntityCulling;
import com.noadsch12.ui.screens.ClientSettingsScreen;
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
    private float tickCounter = 1.0f;
    private static final int UPDATE_INTERVAL = 100; // Alle 5 Sekunden (100 Ticks)
    private static KeyBinding guiKeyBinding;
    public static boolean isMenuAlreadyOpen = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("12th Client - Client initialized!");

        ClientSettingsScreen.registerVars();

        if (TwelfthConfig.check("projectile_ding_enabled")) {
            ClientSettingsScreen.ProjectileDingEnabled = (boolean) TwelfthConfig.getValue("projectile_ding_enabled", "bool");
        } else TwelfthConfig.create("projectile_ding_enabled", "true");

        if (TwelfthConfig.check("jump_to_food_enabled")) {
            ClientSettingsScreen.jumpToFoodEnabled = (boolean) TwelfthConfig.getValue("jump_to_food_enabled", "bool");
        } else TwelfthConfig.create("jump_to_food_enabled", "true");

        if (TwelfthConfig.check("entity_culling_enabled")) {
            ClientSettingsScreen.EntityCullingEnabled = (boolean) TwelfthConfig.getValue("entity_culling_enabled", "bool");
        } else TwelfthConfig.create("entity_culling_enabled", "true");

        if (TwelfthConfig.check("auto_totem_enabled")) {
            ClientSettingsScreen.AutoTotemEnabled = (boolean) TwelfthConfig.getValue("auto_totem_enabled", "bool");
        } else TwelfthConfig.create("auto_totem_enabled", "true");

        if (TwelfthConfig.check("auto_armor_enabled")) {
            ClientSettingsScreen.AutoArmorEnabled = (boolean) TwelfthConfig.getValue("auto_armor_enabled", "bool");
        } else TwelfthConfig.create("auto_armor_enabled", "true");

        if (TwelfthConfig.check("auto_refill_enabled")) {
            ClientSettingsScreen.AutoRefillEnabled = (boolean) TwelfthConfig.getValue("auto_refill_enabled", "bool");
        } else TwelfthConfig.create("auto_refill_enabled", "true");

        if (TwelfthConfig.check("auto_tool_enabled")) {
            ClientSettingsScreen.AutoToolEnabled = (boolean) TwelfthConfig.getValue("auto_tool_enabled", "bool");
        } else TwelfthConfig.create("auto_tool_enabled", "true");

        if (TwelfthConfig.check("better_chat_enabled")) {
            ClientSettingsScreen.BetterChatEnabled = (boolean) TwelfthConfig.getValue("better_chat_enabled", "bool");
        } else TwelfthConfig.create("better_chat_enabled", "true");

        if (TwelfthConfig.check("item_display_enabled")) {
            ClientSettingsScreen.BetterChatEnabled = (boolean) TwelfthConfig.getValue("item_display_enabled", "bool");
        } else TwelfthConfig.create("item_display_enabled", "true");

        if (TwelfthConfig.check("better_scoreboard_enabled")) {
            ClientSettingsScreen.BetterChatEnabled = (boolean) TwelfthConfig.getValue("better_scoreboard_enabled", "bool");
        } else TwelfthConfig.create("better_scoreboard_enabled", "true");

        if (TwelfthConfig.check("hide_totem_enabled")) {
            ClientSettingsScreen.HideTotemAnimEnabled = (boolean) TwelfthConfig.getValue("hide_totem_enabled", "bool");
        } else TwelfthConfig.create("hide_totem_enabled", "true");

        if (TwelfthConfig.check("hide_explosion_enabled")) {
            ClientSettingsScreen.HideExplosionParticlesEnabled = (boolean) TwelfthConfig.getValue("hide_explosion_enabled", "bool");
        } else TwelfthConfig.create("hide_explosion_enabled", "true");

        if (TwelfthConfig.check("show_keystrokes_enabled")) {
            ClientSettingsScreen.ShowKeystrokeSettingsEnabled = (boolean) TwelfthConfig.getValue("show_keystrokes_enabled", "bool");
        } else TwelfthConfig.create("show_keystrokes_enabled", "true");

        if (TwelfthConfig.check("no_tilt_enabled")) {
            ClientSettingsScreen.NoDamageTiltEnabled = (boolean) TwelfthConfig.getValue("no_tilt_enabled", "bool");
        } else TwelfthConfig.create("no_tilt_enabled", "true");

        if (TwelfthConfig.check("aimbot_enabled")) {
            ClientSettingsScreen.AimbotEnabled = (boolean) TwelfthConfig.getValue("aimbot_enabled", "bool");
        } else TwelfthConfig.create("aimbot_enabled", "true");

        if (TwelfthConfig.check("entity_esp_enabled")) {
            ClientSettingsScreen.EntityEspEnabled = (boolean) TwelfthConfig.getValue("entity_esp_enabled", "bool");
        } else TwelfthConfig.create("entity_esp_enabled", "true");

        if (TwelfthConfig.check("trail_index")) {
            ClientSettingsScreen.trailIndex = (int) TwelfthConfig.getValue("trail_index", "int");
        } else TwelfthConfig.create("trail_index", "0");

        if (TwelfthConfig.check("trail_color_index")) {
            ClientSettingsScreen.trailColorIndex = (int) TwelfthConfig.getValue("trail_color_index", "int");
        } else TwelfthConfig.create("trail_color_index", "0");

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
                }
            } else {
                // Only close if we are currently in our "Open" state
                if (isMenuAlreadyOpen) {
                    if (client.currentScreen instanceof ClientSettingsScreen) {
                        client.setScreen(null);
                    }
                    isMenuAlreadyOpen = false;
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
