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

package com.noadsch12.ui.screens;

import com.noadsch12.render.ui.keystrokes.KeystrokesConfig;
import com.noadsch12.render.ui.keystrokes.KeystrokesRenderer;
import com.noadsch12.ui.widgets.ModernButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class KeystrokesSettingsScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;

    public KeystrokesSettingsScreen(Screen parent) {
        super(Text.literal("Keystrokes Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int xMid = this.width / 2;
        int yMid = this.height / 2;
        int bWidth = 150;
        int bHeight = 20;

        // --- Left Column ---

        // Spacebar Toggle
        this.addDrawableChild(new ModernButton(xMid - 155, yMid - 40, bWidth, bHeight,
                Text.literal("Spacebar: " + (KeystrokesConfig.showSpace ? "ON" : "OFF")),
                button -> {
                    KeystrokesConfig.showSpace = !KeystrokesConfig.showSpace;
                    button.setMessage(Text.literal("Spacebar: " + (KeystrokesConfig.showSpace ? "ON" : "OFF")));
                }
        ));

        // Mouse Buttons Toggle (LMB/RMB)
        this.addDrawableChild(new ModernButton(xMid - 155, yMid - 15, bWidth, bHeight,
                Text.literal("Mouse Buttons: " + (KeystrokesConfig.showMouseButtons ? "ON" : "OFF")),
                button -> {
                    KeystrokesConfig.showMouseButtons = !KeystrokesConfig.showMouseButtons;
                    button.setMessage(Text.literal("Mouse Buttons: " + (KeystrokesConfig.showMouseButtons ? "ON" : "OFF")));
                }
        ));

        // --- Right Column ---

        // CPS Toggle
        this.addDrawableChild(new ModernButton(xMid + 5, yMid - 40, bWidth, bHeight,
                Text.literal("CPS Display: " + (KeystrokesConfig.showCPS ? "ON" : "OFF")),
                button -> {
                    KeystrokesConfig.showCPS = !KeystrokesConfig.showCPS;
                    button.setMessage(Text.literal("CPS Display: " + (KeystrokesConfig.showCPS ? "ON" : "OFF")));
                }
        ));

        // Scale Toggle
        this.addDrawableChild(new ModernButton(xMid + 5, yMid - 15, bWidth, bHeight,
                Text.literal("HUD Scale: " + String.format("%.1f", KeystrokesConfig.scale)),
                button -> {
                    KeystrokesConfig.scale = (KeystrokesConfig.scale >= 1.5f) ? 0.5f : KeystrokesConfig.scale + 0.1f;
                    button.setMessage(Text.literal("HUD Scale: " + String.format("%.1f", KeystrokesConfig.scale)));
                }
        ));

        // --- Bottom ---

        // Done Button
        this.addDrawableChild(new ModernButton(xMid - 75, yMid + 25, 150, 20,
                Text.literal("Done"),
                button -> this.close()
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        //this.renderBackground(context, mouseX, mouseY, deltaTicks);

        // Render HUD Preview
        KeystrokesRenderer.render(context);

        context.drawCenteredTextWithShadow(this.textRenderer, "Keystrokes Settings", this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Right-Click & Drag the HUD to move", this.width / 2, 35, 0xFFAA00);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 1 && isHoveringHUD(click.x(), click.y())) {
            this.dragging = true;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 1) {
            this.dragging = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (this.dragging && click.button() == 1) {
            KeystrokesConfig.x += (float) deltaX;
            KeystrokesConfig.y += (float) deltaY;
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    private boolean isHoveringHUD(double mx, double my) {
        // Dynamic box size for dragging detection
        float sizeX = 75 * KeystrokesConfig.scale;
        float sizeY = (KeystrokesConfig.showSpace ? 100 : 75) * KeystrokesConfig.scale;
        return mx >= KeystrokesConfig.x && mx <= KeystrokesConfig.x + sizeX &&
                my >= KeystrokesConfig.y && my <= KeystrokesConfig.y + sizeY;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}