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

package com.noadsch12.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

@Environment(EnvType.CLIENT)
public class EntityCulling {

    private static boolean enabled = true;
    private static int culledCount = 0;
    private static int totalCount = 0;
    private static long lastResetTime = System.currentTimeMillis();

    /**
     * Registers the Entity Culling System
     * Call this method during your client initialization
     */
    public static void register() {
        System.out.println("[EntityCulling] Entity Culling System registriert");
        System.out.println("[EntityCulling] Debug-Modus aktiviert - Statistiken werden geloggt");
    }

    /**
     * Enables or disables entity culling
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
    }

    /**
     * Returns whether entity culling is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks whether an entity should be rendered
     *
     * @param entity The entity to check
     * @param frustum The camera's frustration level
     * @return true if the entity should be rendered, false otherwise
     */
    public static boolean shouldRender(Entity entity, Frustum frustum) {
        totalCount++;

        // Reset statistics every second
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 1000) {
            if (totalCount > 0) {
                System.out.println("[EntityCulling] Last second: " + culledCount + " of " + totalCount + " entities culled (" + (culledCount * 100 / totalCount) + "%)");
            }
            culledCount = 0;
            totalCount = 0;
            lastResetTime = currentTime;
        }

        if (!enabled) {
            return true;
        }

        Box boundingBox = entity.getBoundingBox();
        boolean isVisible = frustum.isVisible(boundingBox);

        if (!isVisible) {
            culledCount++;
        }

        return isVisible;
    }
}