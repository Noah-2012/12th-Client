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

package com.noadsch12.modules.impl.player;
import com.noadsch12.event.events.TickEvent;
import com.noadsch12.event.listeners.TickListener;
import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class FreecamModule extends Module implements TickListener {
    private Vec3d pos;
    private Vec3d prevPos;
    private float yaw, pitch;
    private FakePlayerEntity fakePlayer;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public FreecamModule() {
        super("Freecam", "Freecam", Category.PLAYER, "Let you move the Camera free around", Items.ENDER_EYE);
    }

    @Override
    public void onEnable() {
        if (client.player == null) return;

        // Initialize everything
        pos = client.player.getEyePos();
        prevPos = pos;
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();

        fakePlayer = new FakePlayerEntity();
        fakePlayer.copyFrom(client.player);
        fakePlayer.getAbilities().flying = true; // Prevents some physics jitter
        client.world.addEntity(fakePlayer);
    }

    @Override
    public void onTick(TickEvent.Pre event) {

    }

    @Override
    public void onTick(TickEvent.Post event) {
        float speed = 0.5f;
        Vec3d velocity = Vec3d.ZERO;

        // Use 'this.yaw' so we don't depend on the Camera's state
        Vec3d forward = Vec3d.fromPolar(0, this.yaw);
        Vec3d right = Vec3d.fromPolar(0, this.yaw + 90);

        if (client.options.forwardKey.isPressed()) velocity = velocity.add(forward);
        if (client.options.backKey.isPressed()) velocity = velocity.subtract(forward);
        if (client.options.rightKey.isPressed()) velocity = velocity.add(right);
        if (client.options.leftKey.isPressed()) velocity = velocity.subtract(right);
        if (client.options.jumpKey.isPressed()) velocity = velocity.add(0, 1, 0);
        if (client.options.sneakKey.isPressed()) velocity = velocity.add(0, -1, 0);

        prevPos = pos;
        if (velocity.lengthSquared() > 0) {
            pos = pos.add(velocity.normalize().multiply(speed));
        }

        // Sync the fake player so the world renders around it
        if (fakePlayer != null) {
            fakePlayer.setPosition(pos.x, pos.y, pos.z);
        }
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public void setRotation(float y, float p) { this.yaw = y; this.pitch = p; }
    public Vec3d getInterpolatedPos(float delta) {
        return prevPos.lerp(pos, delta);
    }
    public FakePlayerEntity getFakePlayer() {
        return fakePlayer;
    }
}
