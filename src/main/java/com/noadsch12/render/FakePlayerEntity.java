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

/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package com.noadsch12.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public class FakePlayerEntity extends AbstractClientPlayerEntity {

    public FakePlayerEntity() {
        super(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile());
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
        setPos(player.getEntityPos().x, player.getEntityPos().y, player.getEntityPos().z);
        setRotation(player.getYaw(tickDelta), player.getPitch(tickDelta));
        // this.inventory = player.getInventory();
    }

    public void despawn() {
        remove(RemovalReason.DISCARDED);
    }
}