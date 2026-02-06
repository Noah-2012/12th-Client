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

package com.noadsch12.mixin;

import com.noadsch12.modules.ModuleManager;
import com.noadsch12.modules.impl.render.TrailSettings;
import com.noadsch12.render.TrailRenderer;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class ProjectileTrailMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void spawnArrowTrail(CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();

        // Check if we are on the client side and the projectile is moving
        // 'inGround' is a Yarn field that turns true when the arrow hits a block
        if (!projectile.isOnGround() && ModuleManager.getInstance().getModule("Projectile Trail").isEnabled()) {

            // Spawn the particle. You can change ParticleTypes.END_ROD to
            // something else like ParticleTypes.FLAME or ParticleTypes.SOUL_FIRE_FLAME
            switch (TrailSettings.getCurrentTrailType()) {
                case TOTEM -> {
                    double x = projectile.getX();
                    double y = projectile.getY();
                    double z = projectile.getZ();

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
                case EXPLOSION -> world.addParticleClient(
                        ParticleTypes.EXPLOSION,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        0.0D, 0.0D, 0.0D
                );
                case HEARTS -> world.addParticleClient(
                        ParticleTypes.DAMAGE_INDICATOR,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        0.0D, 0.0D, 0.0D
                );
                case LINE -> TrailRenderer.addPoint(projectile.getUuid(), projectile.getEntityPos());
            }
        }
    }
}