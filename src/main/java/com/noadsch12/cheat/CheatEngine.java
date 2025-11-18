package com.noadsch12.cheat;

import com.noadsch12.util.GithubReleaseFetcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CheatEngine {

    public static boolean espEnabled = false;
    public static boolean fullbrightEnabled = false;

    public static void register() {
        System.out.println("========== CHEATSYSTEM REGISTERED ==========");

        // Commands direkt registrieren!
        registerCommands();
        sendnewCommand();

        // Weltabhängige Logik weiterhin per Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                // z.B. deine Cheat-Features anwenden
            }
        });
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("twelfth")
                    .then(ClientCommandManager.literal("info")
                            .executes(ctx -> {
                                try {
                                    sendInfo();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendMessage("§cAn Error occured: " + e.getMessage());
                                }
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("latest")
                            .then(ClientCommandManager.literal("check")
                                    .executes(ctx -> {
                                        try {
                                            sendLatestCheck();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            sendMessage("§cAn Error occured " + e.getMessage());
                                        }
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("download")
                                    .executes(ctx -> {
                                        try {
                                            downloadLatestVersion();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            sendMessage("§cAn Error occured while downloading: " + e.getMessage());
                                        }
                                        return 1;
                                    })))
            );
        });
    }

    private static void sendInfo() throws Exception {
        sendMessage("§a12th Client§r by Noadsch12");

        String latestVersion = GithubReleaseFetcher.getLatestTag("Noah-2012", "12th-Client");

        sendMessage("You are running Version v1.1.2.");
        if (!latestVersion.equals("v1.1.2")) {
            sendMessage("§eThere is a newer Version of 12th Client.");
            sendMessage("§eType §r§d/twelfth latest check §r§e to see the newest Version.");
        }
    }

    private static void sendLatestCheck() throws Exception {
        String latestVersion = GithubReleaseFetcher.getLatestTag("Noah-2012", "12th-Client");

        sendMessage("The newest Version is " + latestVersion);
        if (!latestVersion.equals("v1.1.2")) {
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

    private static int toggleEsp(boolean state) {
        espEnabled = state;
        return 1;
    }

    private static int toggleFullbright(boolean state) {
        fullbrightEnabled = state;
        return 1;
    }
}
