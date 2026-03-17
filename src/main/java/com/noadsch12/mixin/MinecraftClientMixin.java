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

import blue.endless.jankson.annotation.Nullable;
import com.noadsch12.handlers.ChestStealer;
import com.noadsch12.handlers.PlayerAimbotHandler;
import com.noadsch12.look.ItemHexManager;
import com.noadsch12.mixininterfaces.IMinecraftClient;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.event.EventBus;
import com.noadsch12.event.events.TickEvent;
import com.noadsch12.modules.impl.combat.AutoClickerModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements IMinecraftClient {

    @Shadow
    @Nullable
    public Screen currentScreen;
    @Shadow protected abstract boolean doAttack();
    @Shadow protected abstract void doItemUse();

    @Unique
    private long lastAutoClickTime = 0;

    @Shadow
    @Final
    public Mouse mouse;

    @Shadow
    @Final
    @Mutable
    private Framebuffer framebuffer;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player != null && ModuleManager.getInstance().getModule("Aimbot").isEnabled()) {
            PlayerAimbotHandler.updateAimbot(client);
        }

        AutoClickerModule ac =
                (AutoClickerModule) ModuleManager.getInstance().getModule("Auto Clicker");

        if (ac != null && ac.isEnabled() && this.currentScreen == null) {

            long now = System.currentTimeMillis();

            // delay check
            if (AutoClickerModule.delayEnabled &&
                    now - lastAutoClickTime < AutoClickerModule.delay) {
                return;
            }

            int button = AutoClickerModule.mouseButton; // 0 left, 1 right

            // hold mode check
            if (AutoClickerModule.holdOnly) {
                if (AutoClickerModule.mouseButton == 0 && !client.options.attackKey.isPressed())
                    return;

                if (AutoClickerModule.mouseButton == 1 && !client.options.useKey.isPressed())
                    return;
            }

            // perform click
            if (button == 0) {
                this.doAttack();
            } else {
                this.doItemUse();
            }

            lastAutoClickTime = now;
        }

        ChestStealer.tick();

        ItemHexManager.tick();
    }


    @Inject(method = "tick", at = @At("HEAD"))
    private void tickPre(CallbackInfo ci) {
        EventBus.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        EventBus.post(new TickEvent.Post());
    }

    @Override
    public void _12th_Client$setFramebuffer(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }
}