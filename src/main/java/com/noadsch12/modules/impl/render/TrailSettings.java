/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 */

package com.noadsch12.modules.impl.render;

import com.noadsch12.TwelfthConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

/**
 * Configuration for projectile trail effects
 */
public class TrailSettings {
    private static final String[] TRAIL_NAMES = {"Totem", "Explosion", "Hearts", "Line Trail"};
    private static final String[] TRAIL_COLORS = {"§cRed§r", "§9Blue§r", "§aGreen§r", "§fWhite§r", "§0Black§r"};

    // RGB color values for each trail color
    private static final int[][] TRAIL_COLOR_RGB = {
            {255, 0, 0},     // Red
            {0, 0, 255},     // Blue
            {0, 255, 0},     // Green
            {255, 255, 255}, // White
            {0, 0, 0}        // Black
    };

    private static int trailIndex = 0;
    private static int trailColorIndex = 0;

    /**
     * Trail type enum for cleaner code
     */
    public enum TrailType {
        TOTEM(0),
        EXPLOSION(1),
        HEARTS(2),
        LINE(3);

        private final int index;

        TrailType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static TrailType fromIndex(int index) {
            for (TrailType type : values()) {
                if (type.index == index) return type;
            }
            return TOTEM; // Default
        }
    }

    public static String[] getTrailNames() {
        return TRAIL_NAMES;
    }

    public static String[] getTrailColors() {
        return TRAIL_COLORS;
    }

    public static int getTrailIndex() {
        return trailIndex;
    }

    public static void setTrailIndex(int index) {
        trailIndex = index % TRAIL_NAMES.length;
        TwelfthConfig.setValue("trail_index", String.valueOf(trailIndex));
    }

    public static void cycleTrailIndex() {
        setTrailIndex(trailIndex + 1);
    }

    public static String getCurrentTrailName() {
        return TRAIL_NAMES[trailIndex];
    }

    public static int getTrailColorIndex() {
        return trailColorIndex;
    }

    public static void setTrailColorIndex(int index) {
        trailColorIndex = index % TRAIL_COLORS.length;
        TwelfthConfig.setValue("trail_color_index", String.valueOf(trailColorIndex));
    }

    public static void cycleTrailColorIndex() {
        setTrailColorIndex(trailColorIndex + 1);
    }

    public static String getCurrentTrailColor() {
        return TRAIL_COLORS[trailColorIndex];
    }

    public static boolean isLineTrail() {
        return trailIndex == 3;
    }

    /**
     * Get the current trail type
     */
    public static TrailType getCurrentTrailType() {
        return TrailType.fromIndex(trailIndex);
    }

    /**
     * Check if current trail is a specific type
     */
    public static boolean isTrailType(TrailType type) {
        return trailIndex == type.getIndex();
    }

    /**
     * Render the trail for a projectile based on current settings.
     * This replaces the big if/else chain.
     *
     * @param world The client world
     * @param projectile The projectile entity
     * @param trailRenderer Your TrailRenderer instance (only needed for LINE trail)
     */
    public static void renderTrail(ClientWorld world, Entity projectile, Object trailRenderer) {
        double x = projectile.getX();
        double y = projectile.getY();
        double z = projectile.getZ();

        switch (getCurrentTrailType()) {
            case TOTEM -> renderTotemTrail(world, x, y, z);
            case EXPLOSION -> renderExplosionTrail(world, x, y, z);
            case HEARTS -> renderHeartsTrail(world, x, y, z);
            case LINE -> {
                // Call TrailRenderer.addPoint if you have it
                if (trailRenderer != null) {
                    try {
                        trailRenderer.getClass()
                                .getMethod("addPoint", java.util.UUID.class, net.minecraft.util.math.Vec3d.class)
                                .invoke(trailRenderer, projectile.getUuid(), projectile.getEntityPos());
                    } catch (Exception e) {
                        // Fallback if TrailRenderer not available
                    }
                }
            }
        }
    }

    /**
     * Simplified version without TrailRenderer dependency
     */
    public static void renderTrail(ClientWorld world, Entity projectile) {
        renderTrail(world, projectile, null);
    }

    /**
     * Render totem particle trail
     */
    private static void renderTotemTrail(ClientWorld world, double x, double y, double z) {
        double[][] offsets = {
                {0, 0, 0},
                {0.5, 0, 0}, {-0.5, 0, 0},
                {0, 0.5, 0}, {0, -0.5, 0},
                {0, 0, 0.5}, {0, 0, -0.5}
        };

        for (double[] off : offsets) {
            world.addParticleClient(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    x + off[0], y + off[1], z + off[2],
                    0.0D, 0.0D, 0.0D
            );
        }
    }

    /**
     * Render explosion particle trail
     */
    private static void renderExplosionTrail(ClientWorld world, double x, double y, double z) {
        world.addParticleClient(
                ParticleTypes.EXPLOSION,
                x, y, z,
                0.0D, 0.0D, 0.0D
        );
    }

    /**
     * Render hearts particle trail
     */
    private static void renderHeartsTrail(ClientWorld world, double x, double y, double z) {
        world.addParticleClient(
                ParticleTypes.DAMAGE_INDICATOR,
                x, y, z,
                0.0D, 0.0D, 0.0D
        );
    }

    /**
     * Get the RGB color array for the current trail color.
     * @return int array with [red, green, blue] values (0-255)
     */
    public static int[] getCurrentColorRGB() {
        return TRAIL_COLOR_RGB[trailColorIndex];
    }

    /**
     * Get the red component of the current trail color.
     * @return red value (0-255)
     */
    public static int getRed() {
        return TRAIL_COLOR_RGB[trailColorIndex][0];
    }

    /**
     * Get the green component of the current trail color.
     * @return green value (0-255)
     */
    public static int getGreen() {
        return TRAIL_COLOR_RGB[trailColorIndex][1];
    }

    /**
     * Get the blue component of the current trail color.
     * @return blue value (0-255)
     */
    public static int getBlue() {
        return TRAIL_COLOR_RGB[trailColorIndex][2];
    }

    public static void reset() {
        trailIndex = 0;
        trailColorIndex = 0;
    }
}