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

package com.noadsch12.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.noadsch12.look.ItemHexManager;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.render.entity.EntityCulling;
import com.noadsch12.util.net.GithubReleaseFetcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TwelfthCommand {
    private static boolean commandSent = false;
    private static boolean cullingEnabled = false;

    public static void register() {
        System.out.println("========== COMMANDSYSTEM REGISTERED ==========");

        // Register Commands directly
        registerCommands();

        // World-dependent logic still implemented per tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!commandSent && client.player != null && client.world != null) {
                sendnewCommand();
                commandSent = true;  // stops further execution
            }

            if (ModuleManager.getInstance().getModule("Entity Culling").isEnabled() && !cullingEnabled && client.player != null && client.world != null) {
                EntityCulling.setEnabled(true);
                cullingEnabled = true;
            } else if (!ModuleManager.getInstance().getModule("Entity Culling").isEnabled() && cullingEnabled && client.player != null && client.world != null) {
                EntityCulling.setEnabled(false);
                cullingEnabled = false;
            }
        });
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("twelfth")
                .then(ClientCommandManager.literal("info")
                        .executes(ctx -> {
                            try {
                                sendInfo();
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMessage("§cAn Error occurred: " + e.getMessage());
                            }
                            return 1;
                        }))
                .then(ClientCommandManager.literal("getitem")
                        .then(ClientCommandManager.argument("hex", StringArgumentType.string())
                                .executes(ctx -> {
                                    String hex = StringArgumentType.getString(ctx, "hex").replace("#", "");

                                    // Adjusted for 3 characters
                                    if (hex.length() != 3) {
                                        sendMessage("§cHex value must be exactly 3 characters (e.g., A1F)");
                                        return 0;
                                    }

                                    String result = ItemHexManager.getItem(hex);
                                    sendMessage(result);
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("culling")
                        .then(ClientCommandManager.literal("on")
                                .executes(ctx -> {
                                    try {
                                        EntityCulling.setEnabled(true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sendMessage("§cAn Error occurred " + e.getMessage());
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("off")
                                .executes(ctx -> {
                                    try {
                                        EntityCulling.setEnabled(false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sendMessage("§cAn Error occurred " + e.getMessage());
                                    }
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("latest")
                        .then(ClientCommandManager.literal("check")
                                .executes(ctx -> {
                                    try {
                                        sendLatestCheck();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sendMessage("§cAn Error occurred " + e.getMessage());
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("download")
                                .executes(ctx -> {
                                    try {
                                        downloadLatestVersion();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sendMessage("§cAn Error occurred while downloading: " + e.getMessage());
                                    }
                                    return 1;
                                })))
        ));
    }

    private static void sendInfo() throws Exception {
        sendMessage("§a12th Client§r by Noadsch12");

        String latestVersion = GithubReleaseFetcher.getLatestTag("Noah-2012", "12th-Client");

        sendMessage("You are running Version v1.1.4.");
        if (!latestVersion.equals("v1.1.4")) {
            sendMessage("§eThere is a newer Version of 12th Client.");
            sendMessage("§eType §r§d/twelfth latest check §r§e to see the newest Version.");
        }
    }

    private static void sendLatestCheck() throws Exception {
        String latestVersion = GithubReleaseFetcher.getLatestTag("Noah-2012", "12th-Client");

        sendMessage("The newest Version is " + latestVersion);
        if (!latestVersion.equals("v1.1.4")) {
            sendMessage("§eType §r§d/twelfth latest download §r§eto download the newest Version.");
        } else {
            sendMessage("§aYou are UP-TO-DATE!");
        }
    }

    private static void downloadLatestVersion() throws Exception {
        sendMessage("Download will start in a few seconds.");

        String homeDir = System.getProperty("user.home");
        GithubReleaseFetcher.downloadLatestRelease("Noah-2012", "12th-Client", homeDir + "/Downloads");

        sendMessage("Download finished. You will find the File in your Downloads.");
    }

    private static void sendnewCommand() {
        sendMessage("§e12th Client Commands with §r§d/twelfth ... §r§e available!");
    }

    private static void sendMessage(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(msg), false);
        }
    }
}
