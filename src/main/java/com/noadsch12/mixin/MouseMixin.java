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

import com.noadsch12.event.EventBus;
import com.noadsch12.event.events.MouseScrollEvent;
import com.noadsch12.modules.Module;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.ui.GLWindow;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import com.noadsch12.ui.widgets.ModernButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Element;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

        @Inject(method = "onMouseScroll(JDD)V", at = @At("HEAD"), cancellable = true)
        private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

            MinecraftClient mc = MinecraftClient.getInstance();
            double mouseX = mc.mouse.getX();
            double mouseY = mc.mouse.getY();

            MouseScrollEvent event = new MouseScrollEvent(horizontal, vertical, mouseX, mouseY);
            EventBus.post(event);

            if (event.isCancelled()) {
                ci.cancel();
            }
        }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onAnyClick(long window, MouseInput input, int action, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double scale = mc.getWindow().getScaleFactor();
        double mouseX = mc.mouse.getX() / scale;
        double mouseY = mc.mouse.getY() / scale;
        int button = input.button();

        if (action == 1) {
            // Press — forward to visible windows
            for (Module module : ModuleManager.getInstance().getModules()) {
                GLWindow w = module.getSettingsWindow();
                if (w != null && w.isVisible()) {
                    if (w.onMouseButton(mouseX, mouseY, button)) return;
                }
            }

            // Right-click to open window on ClientSettingsScreen
            if (button == 1 && mc.currentScreen instanceof ClientSettingsScreen screen) {
                for (Element element : screen.children()) {
                    if (element instanceof ModernButton btn) {
                        boolean inBounds = mouseX >= btn.getX()
                                && mouseX <= btn.getX() + btn.getWidth()
                                && mouseY >= btn.getY()
                                && mouseY <= btn.getY() + btn.getHeight();
                        if (inBounds && btn.getLinkedModule() != null) {
                            GLWindow w = btn.getLinkedModule().getSettingsWindow();
                            if (w != null) {
                                boolean nowVisible = !w.isVisible();
                                w.setVisible(nowVisible);
                                if (nowVisible) w.setPosition((int) mouseX + 4, (int) mouseY);
                                return;
                            }
                        }
                    }
                }
            }

        } else if (action == 0) {
            // Release — stop dragging everything
            if (button == 0) {
                for (Module module : ModuleManager.getInstance().getModules()) {
                    GLWindow w = module.getSettingsWindow();
                    if (w != null) {
                        w.onMouseRelease();
                    }
                }
            }
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double scale = mc.getWindow().getScaleFactor();
        double mouseX = x / scale;
        double mouseY = y / scale;

        for (Module module : ModuleManager.getInstance().getModules()) {
            GLWindow w = module.getSettingsWindow();
            if (w != null && w.isVisible()) {
                w.onMouseDragged(mouseX, mouseY);
            }
        }
    }
}
