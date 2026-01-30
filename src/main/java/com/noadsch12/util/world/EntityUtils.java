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

package com.noadsch12.util.world;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public enum EntityUtils
{
    ;
    protected static final MinecraftClient MC = MinecraftClient.getInstance();

    /**
     * Returns a stream of all *attackable* entities in the world.
     *
     * Attackable = LivingEntity with health > 0,
     *               EndCrystalEntity,
     *               ShulkerBulletEntity,
     *               excluding the player and friends.
     */
    public static Stream<Entity> getAttackableEntities()
    {
        return StreamSupport
                .stream(MC.world.getEntities().spliterator(), true)
                .filter(IS_ATTACKABLE);
    }

    public static final Predicate<Entity> IS_ATTACKABLE =
            e -> e != null && !e.isRemoved()
                    && ((e instanceof LivingEntity && ((LivingEntity)e).getHealth() > 0)
                    || e instanceof EndCrystalEntity
                    || e instanceof ShulkerBulletEntity)
                    && e != MC.player;

    /**
     * Returns a stream of *valid animals* (e.g., for animal ESP).
     * Valid = AnimalEntity, not removed, health > 0.
     */
    public static Stream<AnimalEntity> getValidAnimals()
    {
        return StreamSupport
                .stream(MC.world.getEntities().spliterator(), true)
                .filter(AnimalEntity.class::isInstance)
                .map(e -> (AnimalEntity)e)
                .filter(IS_VALID_ANIMAL);
    }

    public static final Predicate<AnimalEntity> IS_VALID_ANIMAL =
            a -> a != null && !a.isRemoved() && a.getHealth() > 0;

    /**
     * Interpolates (lerps) between the entity's previous and current position
     * to get smooth rendering coordinates.
     */
    public static Vec3d getLerpedPos(Entity e, float partialTicks)
    {
        if (e.isRemoved())
            return e.getEntityPos(); // fallback, Entity entfernt

        return e.getLerpedPos(partialTicks); // Vanilla-Lerp
    }

    /**
     * Interpolates between the entity's bounding box in the previous
     * tick and the current tick for smooth bounding boxes.
     */
    public static Box getLerpedBox(Entity e, float partialTicks)
    {
        if (e.isRemoved())
            return e.getBoundingBox();

        Vec3d offset = getLerpedPos(e, partialTicks).subtract(e.getEntityPos());
        return e.getBoundingBox().offset(offset);
    }
}
