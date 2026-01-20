package com.noadsch12.ui.screens;

import com.noadsch12.BasicGlobals;
import com.noadsch12.util.GithubReleaseFetcher;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import java.net.URI;

public class ClientInfoScreen extends Screen {
    private static final Identifier INFO_BACKGROUND = Identifier.of("12th-client", "textures/gui/info_bg.png");
    private final Screen parent;

    // Client Info
    private static final String CLIENT_VERSION = BasicGlobals.CLIENT_VERSION; // Hier deine Version
    private static final String GITHUB_OWNER = "Noah-2012";
    private static final String GITHUB_REPO = "12th-Client";
    private static final String GITHUB_URL = "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO;
    private static final String MC_VERSION = "1.21.10";

    // Update Info
    private String latestVersion = "Checking...";
    private boolean updateAvailable = false;
    private boolean checkingUpdate = true;
    private boolean isDownloading = false;
    private String downloadStatus = "";

    public ClientInfoScreen(Screen parent) {
        super(Text.literal("12th Client Info"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startY = this.height / 2 + 60;
        int spacing = 25;

        // Download Update Button (nur sichtbar wenn Update verfügbar)
        if (updateAvailable && !checkingUpdate) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(isDownloading ? "Downloading..." : "Download Update"),
                    button -> downloadUpdate()
            ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());
            startY += spacing;
        }

        // GitHub Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Open GitHub"),
                button -> openURL(GITHUB_URL)
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // Discord Button (optional)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Join Discord"),
                button -> openURL("https://discord.gg/HMAk5rS8Vg")
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // Close Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.client.setScreen(parent)
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // Start update check in background
        checkForUpdates();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        //this.renderBackground(context, mouseX, mouseY, delta);
        // ^ makes an error because of super.render at the end of the Function

        // Semi-transparent overlay
        context.fill(0, 0, this.width, this.height, 0x88000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Main Info Box
        int boxWidth = 400;
        int boxHeight = 280;
        int boxX = centerX - boxWidth / 2;
        int boxY = centerY - boxHeight / 2;

        // Box Background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xDD000000);

        // Border (manual drawing)
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 2, 0xFF808080); // Top
        context.fill(boxX, boxY + boxHeight - 2, boxX + boxWidth, boxY + boxHeight, 0xFF000000); // Bottom
        //context.fill(boxX, boxY, boxX + 2, boxY + boxHeight, 0xFF00FFFF); // Left
        //context.fill(boxX + boxWidth - 2, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF00FFFF); // Right

        context.fillGradient(
                boxX, boxY,
                boxX + 2, boxY + boxHeight,
                0xFF808080, 0xFF000000
        );

        context.fillGradient(
                boxX + boxWidth - 2, boxY,
                boxX + boxWidth, boxY + boxHeight,
                0xFF808080, 0xFF000000
        );

        // Title
        String title = "12th Client";
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                title,
                centerX,
                boxY + 15,
                0xFF00FFFF
        );

        // Info Text
        int textY = boxY + 40;
        int lineHeight = 12;

        drawInfoLine(context, "Current Version:", CLIENT_VERSION, centerX, textY, 0xFFFFFFFF, 0xFFFFFF00);
        textY += lineHeight;

        drawInfoLine(context, "Minecraft Version:", MC_VERSION, centerX, textY, 0xFFFFFFFF, 0xFF00FF00);
        textY += lineHeight;

        drawInfoLine(context, "Latest Version:", latestVersion, centerX, textY, 0xFFFFFFFF,
                updateAvailable ? 0xFFFF0000 : 0xFF00FF00);
        textY += lineHeight + 5;

        // Update Status
        if (checkingUpdate) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Checking for updates...",
                    centerX,
                    textY,
                    0xFFFFFFFF
            );
        } else if (updateAvailable) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "⚠ New version available!",
                    centerX,
                    textY,
                    0xFFFF0000
            );

            // Download Status
            if (isDownloading && !downloadStatus.isEmpty()) {
                textY += lineHeight;
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        downloadStatus,
                        centerX,
                        textY,
                        0xFFFFFF00
                );
            }
        } else {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "✓ You're up to date!",
                    centerX,
                    textY,
                    0xFF00FF00
            );
        }

        textY += lineHeight + 10;

        // Additional Info
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "§7Created by Noadsch12",
                centerX,
                textY,
                0xFFAAAAAA
        );

        textY += lineHeight;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "§7Special thanks to SniperShot",
                centerX,
                textY,
                0xFFAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawInfoLine(DrawContext context, String label, String value, int centerX, int y, int labelColor, int valueColor) {
        String fullText = label + " " + value;
        int fullWidth = this.textRenderer.getWidth(fullText);
        int labelWidth = this.textRenderer.getWidth(label);

        int startX = centerX - fullWidth / 2;

        context.drawTextWithShadow(this.textRenderer, label, startX, y, labelColor);
        context.drawTextWithShadow(this.textRenderer, value, startX + labelWidth + this.textRenderer.getWidth(" "), y, valueColor);
    }

    private void checkForUpdates() {
        new Thread(() -> {
            try {
                // Hole die neueste Version von GitHub
                String fetchedVersion = GithubReleaseFetcher.getLatestTag(GITHUB_OWNER, GITHUB_REPO);

                if (fetchedVersion != null && !fetchedVersion.isEmpty()) {
                    latestVersion = fetchedVersion;

                    String cleanCurrent = CLIENT_VERSION.replaceFirst("^v", "");
                    String cleanLatest = fetchedVersion.replaceFirst("^v", "");

                    updateAvailable = !cleanCurrent.equals(cleanLatest);
                } else {
                    latestVersion = "Unknown";
                }

                checkingUpdate = false;

            } catch (Exception e) {
                latestVersion = "Error";
                checkingUpdate = false;
                e.printStackTrace();
            }
        }).start();
    }

    private void downloadUpdate() {
        if (isDownloading) return;

        isDownloading = true;
        downloadStatus = "Starting download...";

        new Thread(() -> {
            try {
                String homeDir = System.getProperty("user.home");
                String downloadPath = homeDir + "/Downloads";

                downloadStatus = "Downloading to " + downloadPath + "...";

                boolean success = GithubReleaseFetcher.downloadLatestRelease(
                        GITHUB_OWNER,
                        GITHUB_REPO,
                        downloadPath
                );

                if (success) {
                    downloadStatus = "✓ Download complete! Check your Downloads folder.";
                } else {
                    downloadStatus = "✗ Download failed. Please try again.";
                }

                // Reset nach 5 Sekunden
                Thread.sleep(5000);
                isDownloading = false;
                downloadStatus = "";

            } catch (Exception e) {
                downloadStatus = "✗ Error: " + e.getMessage();
                isDownloading = false;
                e.printStackTrace();
            }
        }).start();
    }

    private void openURL(String url) {
        try {
            Util.getOperatingSystem().open(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}