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

package com.noadsch12.render.ui;

import com.noadsch12.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class CompassHud {

    private static final List<Waypoint> waypoints = new ArrayList<>();

    public static void render(DrawContext context) {
        if (!ModuleManager.getInstance().getModule("Compass HUD").isEnabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();

        // Compass dimensions
        int compassWidth = 200;
        int compassHeight = 20;
        int compassX = (screenWidth - compassWidth) / 2;
        int compassY = 10;

        // Get player yaw (rotation)
        float yaw = MathHelper.wrapDegrees(client.player.getYaw());

        // Background removed for invisible effect

        // Draw compass bar
        renderCompassBar(context, compassX, compassY, compassWidth, compassHeight, yaw);

        // Draw center indicator (thinner)
        int centerX = compassX + compassWidth / 2;
        context.fill(centerX, compassY - 5, centerX + 1, compassY + compassHeight + 5, 0xFFFFFFFF);

        // Draw waypoints
        if (client.player != null) {
            renderWaypoints(context, compassX, compassY, compassWidth, compassHeight,
                    yaw, client.player.getX(), client.player.getZ());
        }
    }

    private static void renderCompassBar(DrawContext context, int x, int y, int width, int height, float yaw) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float[] directionAngles = {0, 45, 90, 135, 180, -135, -90, -45};

        for (int i = 0; i < directions.length; i++) {
            float angle = directionAngles[i];
            float diff = MathHelper.wrapDegrees(angle - yaw);

            // Calculate position on compass bar
            float posRatio = diff / 90.0f; // 90 degrees visible on each side

            if (Math.abs(posRatio) <= 1.0f) {
                int dirX = x + width / 2 + (int)(posRatio * width / 2);

                // Calculate fade based on distance from center
                float fadeAmount = 1.0f - Math.abs(posRatio);
                int alpha = (int)(255 * fadeAmount);

                // Color based on cardinal directions with fade
                int baseColor = (i % 2 == 0) ? 0xFF5555 : 0xAAAAAA;
                int color = (alpha << 24) | baseColor;

                // Draw direction marker
                context.drawText(MinecraftClient.getInstance().textRenderer,
                        directions[i], dirX - 4, y + 5, color, true);
            }
        }

        // Draw degree markers with fade
        for (int deg = -180; deg <= 180; deg += 15) {
            float diff = MathHelper.wrapDegrees(deg - yaw);
            float posRatio = diff / 90.0f;

            if (Math.abs(posRatio) <= 1.0f) {
                int markX = x + width / 2 + (int)(posRatio * width / 2);

                // Calculate fade for degree markers
                float fadeAmount = 1.0f - Math.abs(posRatio);
                int alpha = (int)(128 * fadeAmount);
                int color = (alpha << 24) | 0xFFFFFF;

                context.fill(markX, y + 1, markX + 1, y + 3, color);
            }
        }
    }

    private static void renderWaypoints(DrawContext context, int x, int y, int width, int height,
                                        float yaw, double playerX, double playerZ) {
        for (Waypoint wp : waypoints) {
            double dx = wp.x - playerX;
            double dz = wp.z - playerZ;

            // Calculate angle to waypoint
            float angle = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90;
            float diff = MathHelper.wrapDegrees(angle - yaw);

            float posRatio = diff / 90.0f;

            if (Math.abs(posRatio) <= 1.0f) {
                int wpX = x + width / 2 + (int)(posRatio * width / 2);

                // Calculate fade for waypoints
                float fadeAmount = 1.0f - Math.abs(posRatio);
                int alpha = (int)(255 * fadeAmount);
                int markerColor = (alpha << 24) | 0x55FF55;

                // Draw waypoint marker
                context.fill(wpX - 1, y - 3, wpX + 1, y, markerColor);

                // Draw waypoint name
                String name = wp.name;
                int distance = (int)Math.sqrt(dx * dx + dz * dz);
                String label = name + " (" + distance + "m)";

                int textColor = (alpha << 24) | 0x55FF55;
                int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(label);
                context.drawText(MinecraftClient.getInstance().textRenderer,
                        label, wpX - textWidth / 2, y + height + 3,
                        textColor, true);
            }
        }
    }

    // Waypoint class
    public static class Waypoint {
        public final String name;
        public final double x;
        public final double y;
        public final double z;

        public Waypoint(String name, double x, double y, double z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Public API to add/remove waypoints
    public static void addWaypoint(String name, double x, double y, double z) {
        waypoints.add(new Waypoint(name, x, y, z));
    }

    public static void removeWaypoint(String name) {
        waypoints.removeIf(wp -> wp.name.equals(name));
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static List<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
}