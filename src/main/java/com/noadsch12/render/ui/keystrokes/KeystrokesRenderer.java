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

package com.noadsch12.render.ui.keystrokes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class KeystrokesRenderer {

    private static final List<Long> leftClicks = new ArrayList<>();
    private static final List<Long> rightClicks = new ArrayList<>();

    // Track previous state to detect "Initial Press" only
    private static boolean wasLmbDown = false;
    private static boolean wasRmbDown = false;

    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        context.getMatrices().pushMatrix(); // Rule: .pushMatrix()

        context.getMatrices().translate(KeystrokesConfig.x, KeystrokesConfig.y);
        context.getMatrices().scale(KeystrokesConfig.scale, KeystrokesConfig.scale);

        // 1. WASD Keys
        drawKey(context, 23, 0, 20, 20, "W", mc.options.forwardKey.isPressed());
        drawKey(context, 1, 22, 20, 20, "A", mc.options.leftKey.isPressed());
        drawKey(context, 23, 22, 20, 20, "S", mc.options.backKey.isPressed());
        drawKey(context, 45, 22, 20, 20, "D", mc.options.rightKey.isPressed());

        int currentY = 44;

        // 2. LMB and RMB with "Edge Detection" for CPS
        if (KeystrokesConfig.showMouseButtons) {
            boolean lmbPressed = mc.options.attackKey.isPressed();
            boolean rmbPressed = mc.options.useKey.isPressed();

            // Only record click if it was NOT pressed last frame, but IS pressed now
            if (lmbPressed && !wasLmbDown) {
                recordClick(leftClicks);
            }
            if (rmbPressed && !wasRmbDown) {
                recordClick(rightClicks);
            }

            // Update states for the next frame
            wasLmbDown = lmbPressed;
            wasRmbDown = rmbPressed;

            drawMouseButton(context, 1, currentY, 31, 26, "LMB", getCPS(leftClicks), lmbPressed);
            drawMouseButton(context, 34, currentY, 31, 26, "RMB", getCPS(rightClicks), rmbPressed);

            currentY += 28;
        }

        // 3. Spacebar
        if (KeystrokesConfig.showSpace) {
            drawKey(context, 1, currentY, 64, 15, "____", mc.options.jumpKey.isPressed());
        }

        context.getMatrices().popMatrix(); // Rule: .popMatrix()
    }

    private static void drawKey(DrawContext context, int x, int y, int w, int h, String label, boolean pressed) {
        int alpha = (int) (KeystrokesConfig.opacity * 255);
        int bgColor = pressed ? (alpha << 24 | 0xFFFFFF) : (alpha << 24);
        int textColor = pressed ? 0xFF000000 : KeystrokesConfig.color;

        context.fill(x, y, x + w, y + h, bgColor);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, label, x + (w / 2), y + (h / 2) - 4, textColor);
    }

    private static void drawMouseButton(DrawContext context, int x, int y, int w, int h, String label, int cps, boolean pressed) {
        int alpha = (int) (KeystrokesConfig.opacity * 255);
        int bgColor = pressed ? (alpha << 24 | 0xFFFFFF) : (alpha << 24);
        int textColor = pressed ? 0xFF000000 : KeystrokesConfig.color;

        context.fill(x, y, x + w, y + h, bgColor);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, label, x + (w / 2), y + 4, textColor);

        if (KeystrokesConfig.showCPS) {
            context.getMatrices().pushMatrix();
            float cpsX = x + (w / 2f);
            float cpsY = y + 15;

            // Apply a 0.7x scale to make CPS text smaller
            context.getMatrices().translate(cpsX, cpsY);
            context.getMatrices().scale(0.7f, 0.7f);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, cps + " CPS", 0, 0, textColor);
            context.getMatrices().popMatrix();
        }
    }

    private static void recordClick(List<Long> clicks) {
        clicks.add(System.currentTimeMillis());
    }

    private static int getCPS(List<Long> clicks) {
        long now = System.currentTimeMillis();
        clicks.removeIf(timestamp -> now - timestamp > 1000);
        return clicks.size();
    }
}