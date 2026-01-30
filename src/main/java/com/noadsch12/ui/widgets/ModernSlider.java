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

import com.noadsch12.annotations.NotUpdated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import java.util.function.Consumer;

import static com.noadsch12.BasicGlobals.ARIAL_FONT;

@NotUpdated
public class ModernSlider extends SliderWidget {
    private static final Style MODERN_STYLE = Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT));

    private static final int TEXT_PADDING = 8;
    private float hoverProgress = 0.0f;

    private Text customTooltipText;
    private float tooltipAlpha = 0.0f;
    private long hoverStartTime = 0L;
    private static final long FADE_DELAY_MS = 175;
    private static final float FADE_SPEED = 0.05f;

    private final Consumer<Double> onValueChange;
    private final String prefix;

    public ModernSlider(int x, int y, int width, int height, Text prefix, double value, Consumer<Double> onValueChange) {
        super(x, y, width, height, prefix, value);
        this.prefix = prefix.getString();
        this.onValueChange = onValueChange;
        updateMessage();
    }

    public ModernSlider withTooltip(String text) {
        this.customTooltipText = Text.of(text);
        return this;
    }

    @Override
    protected void updateMessage() {
        // Formats the message to show Prefix: Value%
        int percentage = (int) (this.value * 100);
        setMessage(Text.of(prefix + ": " + percentage + "%"));
    }

    @Override
    protected void applyValue() {
        if (onValueChange != null) {
            onValueChange.accept(this.value);
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Animation Logic
        if (this.isSelected()) {
            hoverProgress = Math.min(1.0f, hoverProgress + (delta * 0.1f));
        } else {
            hoverProgress = Math.max(0.0f, hoverProgress - (delta * 0.1f));
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        // 2. Colors
        int backgroundColor = 0x15000000;
        int borderColor = this.isSelected() ? 0xFFFFFFFF : 0x80707070;
        int textColor = this.active ? (this.isSelected() ? 0xFFFFF500 : 0xFFFFFFFF) : 0xFFA0A0A0;
        int handleColor = this.isSelected() ? 0xFFFFF500 : 0xFFFFFFFF;

        // 3. Draw Background & Track
        context.fill(x + 2, y, x + w - 2, y + h, backgroundColor);
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, backgroundColor);
        context.fill(x, y + 2, x + w, y + h - 2, backgroundColor);

        // Visual "Progress" fill (Subtle accent)
        int progressWidth = (int)(this.value * (w - 4));
        context.fill(x + 2, y + 2, x + 2 + progressWidth, y + h - 2, 0x20FFFFFF);

        drawEnhancedRoundedOutline(context, x, y, w, h, borderColor);

        // 4. Draw Modern Handle (Vertical Bar)
        int handleX = x + 2 + (int) (this.value * (w - 8));
        context.fill(handleX, y + 2, handleX + 4, y + h - 2, handleColor);

        Text styledMessage = this.getMessage().copy().setStyle(MODERN_STYLE);

        context.drawCenteredTextWithShadow(
                client.textRenderer,
                styledMessage,
                x + w / 2,
                y + (h - 8) / 2,
                textColor
        );

        // 6. Tooltip Logic
        if (this.isMouseOver(mouseX, mouseY) && customTooltipText != null) {
            if (hoverStartTime == 0L) hoverStartTime = System.currentTimeMillis();
            long hoverDuration = System.currentTimeMillis() - hoverStartTime;

            if (hoverDuration > FADE_DELAY_MS) {
                tooltipAlpha = Math.min(1.0f, tooltipAlpha + (delta * FADE_SPEED));
                renderCustomTooltip(context, mouseX, mouseY, tooltipAlpha);
            }
        } else {
            hoverStartTime = 0L;
            tooltipAlpha = 0.0f;
        }
    }

    private void renderCustomTooltip(DrawContext context, int mouseX, int mouseY, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        String[] lines = customTooltipText.getString().split("\n");
        int padding = 5;
        int lineSpacing = 5;
        int lineHeight = 4;
        Style modernStyle = Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT));

        int maxWidth = 0;
        for (String line : lines) {
            int w = client.textRenderer.getWidth(Text.literal(line).setStyle(modernStyle));
            if (w > maxWidth) maxWidth = w;
        }

        int totalWidth = maxWidth + (padding * 2);
        int totalHeight = (lines.length * lineHeight) + ((lines.length - 1) * lineSpacing) + (padding * 2);

        int tx = mouseX + 12;
        int ty = mouseY - 12;
        if (tx + totalWidth > context.getScaledWindowWidth()) tx = mouseX - totalWidth - 12;
        if (ty + totalHeight > context.getScaledWindowHeight()) ty = context.getScaledWindowHeight() - totalHeight;

        int alphaInt = (int)(alpha * 255);
        int bgColor = (alphaInt << 24) | 0x121212;
        int borderColor = (alphaInt << 24) | 0xFFFFFF;
        int textColor = (alphaInt << 24) | 0xE0E0E0;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, 0);

        context.fill(tx + 1, ty, tx + totalWidth - 1, ty + totalHeight, bgColor);
        drawEnhancedRoundedOutline(context, tx, ty, totalWidth, totalHeight, borderColor);

        int currentY = ty + padding - 2;
        for (String lineText : lines) {
            Text styledLine = Text.literal(lineText).setStyle(modernStyle);
            context.drawText(client.textRenderer, styledLine, tx + padding, currentY, textColor, false);
            currentY += lineHeight + lineSpacing;
        }

        context.getMatrices().popMatrix();
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
}