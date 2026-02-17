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

package com.noadsch12.ui.widgets;

import blue.endless.jankson.annotation.Nullable;
import com.noadsch12.annotations.NotUpdated;
import com.noadsch12.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;

import static com.noadsch12.BasicGlobals.ARIAL_FONT;

@NotUpdated
public class ModernButton extends ButtonWidget {
    //private static final Identifier ARIAL_FONT = Identifier.of("12th-client", "arial");
    private static final int TEXT_PADDING = 8;

    private float hoverProgress = 0.0f;

    private Text customTooltipText;
    @Nullable private Module linkedModule;

    private float scrollOffset = 0.0f;
    private float scrollTimer = 0.0f;
    private float tooltipAlpha = 0.0f;
    private boolean scrollDirection = true;
    private static final float SCROLL_SPEED = 20.0f;
    private static final float PAUSE_DURATION = 30.0f;

    private long hoverStartTime = 0L;
    private static final long FADE_DELAY_MS = 175;
    private static final float FADE_SPEED = 0.05f;

    public ModernButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    public ModernButton withTooltip(String text) {
        this.customTooltipText = Text.of(text);
        return this;
    }

    public ModernButton withModule(Module module) {
        this.linkedModule = module;
        return this;
    }

    public boolean shouldRenderTooltip(int mouseX, int mouseY) {
        return this.isMouseOver(mouseX, mouseY) && customTooltipText != null;
    }

    public Text getTooltip() {
        return customTooltipText;
    }

    public float getTooltipAlpha() {
        return tooltipAlpha;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Animations-Logik (Berechnung des Fortschritts)
        // Wenn die Maus drauf ist, steigt der Wert, sonst sinkt er
        if (this.isSelected()) {
            hoverProgress = Math.min(1.0f, hoverProgress + (delta * 0.1f)); // Geschwindigkeit anpassen
        } else {
            hoverProgress = Math.max(0.0f, hoverProgress - (delta * 0.1f));
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        // 2. Farben definieren
        int backgroundColor = this.isSelected() ? 0x30FFFFFF : 0x15000000;
        int borderColor = this.isSelected() ? 0xFFFFFFFF : 0x80707070;
        int textColor = this.active ? (this.isSelected() ? 0xFFFFF500 : 0xFFFFFFFF) : 0xFFA0A0A0;

        // 3. Hintergrund & Rahmen (wie vorher)
        context.fill(x + 2, y, x + w - 2, y + h, backgroundColor);
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, backgroundColor);
        context.fill(x, y + 2, x + w, y + h - 2, backgroundColor);
        drawEnhancedRoundedOutline(context, x, y, w, h, borderColor);

        // 4. DIE SLIDE-ANIMATION (Rechts nach Links)
        if (hoverProgress > 0) {
            int slideWidth = 40;
            int totalDistance = w + slideWidth;
            int currentX = x + w - (int)(hoverProgress * totalDistance);

            context.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);
            int slideColor = 0x30FFFFFF;
            context.fill(currentX, y, currentX + slideWidth, y + h, slideColor);
            context.disableScissor();
        }

        // 5. Text rendern
        Text styledMessage = this.getMessage().copy().setStyle(
                Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT))
        );

        int textWidth = client.textRenderer.getWidth(styledMessage);
        int availableWidth = w - (TEXT_PADDING * 2);

        // Prüfen ob Text zu lang ist
        if (textWidth > availableWidth) {
            // Text ist zu lang -> Scrolling aktivieren
            updateScrolling(delta, textWidth, availableWidth);
            renderScrollingText(context, client, styledMessage, x, y, w, h, textColor, availableWidth);
        } else {
            // Text passt -> normal zentriert rendern
            scrollOffset = 0.0f;
            scrollTimer = 0.0f;
            context.drawCenteredTextWithShadow(
                    client.textRenderer,
                    styledMessage,
                    x + w / 2,
                    y + (h - 8) / 2,
                    textColor
            );
        }

        if (this.isMouseOver(mouseX, mouseY) && customTooltipText != null) {
            // Update timing
            if (hoverStartTime == 0L) {
                hoverStartTime = System.currentTimeMillis();
            }

            // Calculate how long we've been hovering
            long hoverDuration = System.currentTimeMillis() - hoverStartTime;

            if (hoverDuration > FADE_DELAY_MS) {
                // Increase alpha once delay is passed
                tooltipAlpha = Math.min(1.0f, tooltipAlpha + (delta * FADE_SPEED));
                // THIS GETS HANDLED BY THE SCREEN RENDER METHOD
                //renderCustomTooltip(context, mouseX, mouseY, tooltipAlpha);
            }
        } else {
            // Reset when mouse leaves
            hoverStartTime = 0L;
            tooltipAlpha = 0.0f;
        }
    }

    public void renderCustomTooltip(DrawContext context, int mouseX, int mouseY, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Split the text by \n
        String[] lines = customTooltipText.getString().split("\n");
        int padding = 5;
        int lineSpacing = 5;
        int lineHeight = 4;

        Style modernStyle = Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT));

        int maxWidth = 0;
        for (String line : lines) {
            // We convert the string to a Text object with the style BEFORE measuring
            int w = client.textRenderer.getWidth(Text.literal(line).setStyle(modernStyle));
            if (w > maxWidth) maxWidth = w;
        }

        int totalWidth = maxWidth + (padding * 2);
        int totalHeight = (lines.length * lineHeight) + ((lines.length - 1) * lineSpacing) + (padding * 2);

        // 3. Positioning
        int tx = mouseX + 12;
        int ty = mouseY - 12;
        if (tx + totalWidth > context.getScaledWindowWidth()) tx = mouseX - totalWidth - 12;
        if (ty + totalHeight > context.getScaledWindowHeight()) ty = context.getScaledWindowHeight() - totalHeight;

        // 4. Color Calculation (ARGB)
        int alphaInt = (int)(alpha * 255);
        int bgColor = (alphaInt << 24) | 0x121212; // Very dark gray
        int borderColor = (alphaInt << 24) | 0xFFFFFF; // White border
        int textColor = (alphaInt << 24) | 0xE0E0E0; // Off-white text

        context.getMatrices().pushMatrix();

        context.getMatrices().translate(0, 0);

        // 5. Draw Background & Border
        context.fill(tx + 1, ty, tx + totalWidth - 1, ty + totalHeight, bgColor);
        drawEnhancedRoundedOutline(context, tx, ty, totalWidth, totalHeight, borderColor);

        // 6. Draw each line
        int currentY = ty + padding - 2;
        for (String lineText : lines) {
            // Apply the Arial font to each line individually
            Text styledLine = Text.literal(lineText).setStyle(
                    Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT))
            );

            context.drawText(
                    client.textRenderer,
                    styledLine,
                    tx + padding,
                    currentY,
                    textColor,
                    false
            );
            currentY += lineHeight + lineSpacing;
        }

        context.getMatrices().popMatrix();
    }

    private void updateScrolling(float delta, int textWidth, int availableWidth) {
        int maxScroll = textWidth - availableWidth;

        scrollTimer += delta;

        if (scrollTimer < PAUSE_DURATION) {
            // Pause am Anfang/Ende
            return;
        }

        // Scroll-Animation
        float scrollDelta = (SCROLL_SPEED * delta) / 20.0f; // Normalisiert auf 20 FPS

        if (scrollDirection) {
            // Nach links scrollen
            scrollOffset += scrollDelta;
            if (scrollOffset >= maxScroll) {
                scrollOffset = maxScroll;
                scrollDirection = false;
                scrollTimer = 0.0f; // Pause starten
            }
        } else {
            // Nach rechts scrollen
            scrollOffset -= scrollDelta;
            if (scrollOffset <= 0) {
                scrollOffset = 0;
                scrollDirection = true;
                scrollTimer = 0.0f; // Pause starten
            }
        }
    }

    private void renderScrollingText(DrawContext context, MinecraftClient client, Text text,
                                     int x, int y, int w, int h, int color, int availableWidth) {
        // Scissor aktivieren um Text außerhalb des Buttons abzuschneiden
        context.enableScissor(x + TEXT_PADDING, y, x + w - TEXT_PADDING, y + h);

        // Text mit Offset zeichnen (linksbündig statt zentriert)
        int textX = x + TEXT_PADDING - (int)scrollOffset;
        int textY = y + (h - 8) / 2;

        context.drawTextWithShadow(
                client.textRenderer,
                text,
                textX,
                textY,
                color
        );

        context.disableScissor();
    }

    private void drawEnhancedRoundedOutline(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x + 2, y, x + w - 2, y + 1, color);
        context.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
        context.fill(x, y + 2, x + 1, y + h - 2, color);
        context.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        context.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
        context.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    // Add to ModernButton
    public Module getLinkedModule() {
        return linkedModule;
    }
}