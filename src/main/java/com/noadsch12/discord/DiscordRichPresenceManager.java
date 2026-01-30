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

package com.noadsch12.discord;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class DiscordRichPresenceManager {
    private static final String APPLICATION_ID = "1449350308814786645";
    private static boolean initialized = false;
    private static long startTimestamp;

    public static void init() {
        if (initialized) return;

        try {
            // Event Handlers (optional, für Callbacks)
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                    .setReadyEventHandler(user -> {
                        System.out.println("Discord RPC verbunden mit: " + user.username + "#" + user.discriminator);
                    })
                    .build();

            // Discord initialisieren
            DiscordRPC.discordInitialize(APPLICATION_ID, handlers, true);
            initialized = true;
            startTimestamp = System.currentTimeMillis();

            // Initiale Presence setzen
            updatePresence("In Menu", null);

            System.out.println("Discord Rich Presence initialized succesfull");
        } catch (Exception e) {
            System.err.println("Fehler beim Initialisieren von Discord RPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updatePresence(String details, String state) {
        if (!initialized) return;

        try {
            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state != null ? state : "");
            builder.setDetails(details);

            // Startzeit (zeigt "seit X Minuten" an)
            builder.setStartTimestamps(startTimestamp / 1000);

            // Optional: Large Image (muss in Discord Developer Portal hochgeladen sein)
            builder.setBigImage("minecraft_icon", "12th Client");

            // Optional: Small Image
            // builder.setSmallImage("status_icon", "Online");

            DiscordRPC.discordUpdatePresence(builder.build());
        } catch (Exception e) {
            System.err.println("Error while updating Discord Presence: " + e.getMessage());
        }
    }

    public static void updateForInGame() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null) {
            String worldName = "Singleplayer";
            String details = "Plays Minecraft";

            if (client.getCurrentServerEntry() != null) {
                ServerInfo serverInfo = client.getCurrentServerEntry();
                worldName = serverInfo.name;
                details = "On Server: " + serverInfo.address;
            } else if (client.isInSingleplayer()) {
                if (client.getServer() != null && client.getServer().getSaveProperties() != null) {
                    worldName = client.getServer().getSaveProperties().getLevelName();
                }
                details = "Singleplayer";
            }

            updatePresence(details, worldName);
        } else {
            updatePresence("In Menu", "Mainmenu");
        }
    }

    public static void tick() {
        if (!initialized) return;

        try {
            // Discord Callbacks verarbeiten
            DiscordRPC.discordRunCallbacks();
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten von Discord Callbacks: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (!initialized) return;

        try {
            DiscordRPC.discordShutdown();
            initialized = false;
            System.out.println("Discord Rich Presence beendet.");
        } catch (Exception e) {
            System.err.println("Fehler beim Herunterfahren von Discord RPC: " + e.getMessage());
        }
    }
}