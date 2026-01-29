package com.noadsch12.ui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;

import static com.noadsch12.BasicGlobals.ARIAL_FONT;

public class ModernButtonNew extends ButtonWidget {
    private static final int TEXT_PADDING = 8;
    private static final float CORNER_RADIUS = 8.0f;

    private float hoverProgress = 0.0f;
    private Text customTooltipText;

    private float scrollOffset = 0.0f;
    private float scrollTimer = 0.0f;
    private float tooltipAlpha = 0.0f;
    private boolean scrollDirection = true;
    private static final float SCROLL_SPEED = 20.0f;
    private static final float PAUSE_DURATION = 30.0f;

    private long hoverStartTime = 0L;
    private static final long FADE_DELAY_MS = 175;
    private static final float FADE_SPEED = 0.05f;

    public ModernButtonNew(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    public ModernButtonNew withTooltip(String text) {
        this.customTooltipText = Text.of(text);
        return this;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Update hover animation
        if (this.isSelected()) {
            hoverProgress = Math.min(1.0f, hoverProgress + (delta * 0.1f));
        } else {
            hoverProgress = Math.max(0.0f, hoverProgress - (delta * 0.1f));
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        // Colors with smooth transitions
        int bgAlpha = this.isSelected() ?
                (int)((0.19f + (hoverProgress * 0.06f)) * 255) : (int)(0.08f * 255);
        int borderAlpha = this.isSelected() ? 255 : (int)(0.5f * 255);
        int textColor = this.active ? (this.isSelected() ? 0xFFFFF500 : 0xFFFFFFFF) : 0xFFA0A0A0;

        int backgroundColor = (bgAlpha << 24) | 0x000000;
        int borderColor = (borderAlpha << 24) | 0xFFFFFF;

        // Draw smooth rounded background
        drawSmoothRoundedRect(context, x, y, w, h, CORNER_RADIUS, backgroundColor);

        // Draw smooth border
        drawSmoothRoundedBorder(context, x, y, w, h, CORNER_RADIUS, borderColor);

        // Slide animation overlay
        if (hoverProgress > 0) {
            int slideWidth = 40;
            int totalDistance = w + slideWidth;
            float slideX = x + w - (hoverProgress * totalDistance);

            int slideAlpha = (int)(hoverProgress * 0.19f * 255);
            drawSmoothGradientSlide(context, (int)slideX, y, slideWidth, h, slideAlpha);
        }

        // Render text
        Text styledMessage = this.getMessage().copy().setStyle(
                Style.EMPTY.withFont(new StyleSpriteSource.Font(ARIAL_FONT))
        );

        int textWidth = client.textRenderer.getWidth(styledMessage);
        int availableWidth = w - (TEXT_PADDING * 2);

        if (textWidth > availableWidth) {
            updateScrolling(delta, textWidth, availableWidth);
            renderScrollingText(context, client, styledMessage, x, y, w, h, textColor, availableWidth);
        } else {
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

        // Tooltip handling
        if (this.isMouseOver(mouseX, mouseY) && customTooltipText != null) {
            if (hoverStartTime == 0L) {
                hoverStartTime = System.currentTimeMillis();
            }

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

    private void drawSmoothRoundedRect(DrawContext context, int x, int y, int width, int height,
                                       float radius, int color) {
        int r = (int)radius;

        // Main body rectangles
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + r, y + height - r, color);
        context.fill(x + width - r, y + r, x + width, y + height - r, color);

        // Draw rounded corners with anti-aliasing effect using multiple layers
        int segments = 16;

        // Top-left corner
        drawFilledCorner(context, x + r, y + r, r, 180, 270, segments, color);

        // Top-right corner
        drawFilledCorner(context, x + width - r, y + r, r, 270, 360, segments, color);

        // Bottom-right corner
        drawFilledCorner(context, x + width - r, y + height - r, r, 0, 90, segments, color);

        // Bottom-left corner
        drawFilledCorner(context, x + r, y + height - r, r, 90, 180, segments, color);
    }

    private void drawFilledCorner(DrawContext context, int centerX, int centerY, int radius,
                                  float startAngle, float endAngle, int segments, int color) {
        // Draw the corner as a series of small rectangles approximating a curve
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * i / segments);
            float angle2 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * (i + 1) / segments);

            int x1 = centerX + (int)(Math.cos(angle1) * radius);
            int y1 = centerY + (int)(Math.sin(angle1) * radius);
            int x2 = centerX + (int)(Math.cos(angle2) * radius);
            int y2 = centerY + (int)(Math.sin(angle2) * radius);

            // Fill triangle from center to edge
            fillTriangle(context, centerX, centerY, x1, y1, x2, y2, color);
        }
    }

    private void fillTriangle(DrawContext context, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // Find bounding box
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        // Fill the triangle by checking each pixel
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInTriangle(x, y, x1, y1, x2, y2, x3, y3)) {
                    context.fill(x, y, x + 1, y + 1, color);
                }
            }
        }
    }

    private boolean isPointInTriangle(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        float d1 = sign(px, py, x1, y1, x2, y2);
        float d2 = sign(px, py, x2, y2, x3, y3);
        float d3 = sign(px, py, x3, y3, x1, y1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private float sign(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
        return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
    }

    private void drawSmoothRoundedBorder(DrawContext context, int x, int y, int width, int height,
                                         float radius, int color) {
        int r = (int)radius;

        // Draw straight edges
        // Top edge
        context.fill(x + r, y, x + width - r, y + 1, color);
        // Bottom edge
        context.fill(x + r, y + height - 1, x + width - r, y + height, color);
        // Left edge
        context.fill(x, y + r, x + 1, y + height - r, color);
        // Right edge
        context.fill(x + width - 1, y + r, x + width, y + height - r, color);

        // Draw rounded corners
        int segments = 16;

        // Top-left corner
        drawCornerBorder(context, x + r, y + r, r, 180, 270, segments, color);

        // Top-right corner
        drawCornerBorder(context, x + width - r, y + r, r, 270, 360, segments, color);

        // Bottom-right corner
        drawCornerBorder(context, x + width - r, y + height - r, r, 0, 90, segments, color);

        // Bottom-left corner
        drawCornerBorder(context, x + r, y + height - r, r, 90, 180, segments, color);
    }

    private void drawCornerBorder(DrawContext context, int centerX, int centerY, int radius,
                                  float startAngle, float endAngle, int segments, int color) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * i / segments);
            float angle2 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * (i + 1) / segments);

            int x1 = centerX + (int)(Math.cos(angle1) * radius);
            int y1 = centerY + (int)(Math.sin(angle1) * radius);
            int x2 = centerX + (int)(Math.cos(angle2) * radius);
            int y2 = centerY + (int)(Math.sin(angle2) * radius);

            // Draw line segment
            drawLine(context, x1, y1, x2, y2, color);
        }
    }

    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            context.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void drawSmoothGradientSlide(DrawContext context, int x, int y, int width, int height, int maxAlpha) {
        // Draw gradient using multiple vertical strips
        int strips = 20;
        for (int i = 0; i < strips; i++) {
            float progress = (float)i / strips;
            int alpha;

            if (progress < 0.5f) {
                // Fade in
                alpha = (int)(maxAlpha * (progress * 2));
            } else {
                // Fade out
                alpha = (int)(maxAlpha * ((1.0f - progress) * 2));
            }

            int stripWidth = width / strips;
            int stripX = x + (i * stripWidth);
            int color = (alpha << 24) | 0xFFFFFF;

            context.fill(stripX, y, stripX + stripWidth + 1, y + height, color);
        }
    }

    private void renderCustomTooltip(DrawContext context, int mouseX, int mouseY, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();

        String[] lines = customTooltipText.getString().split("\n");
        int padding = 8;
        int lineSpacing = 5;
        int lineHeight = 10;

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

        // Draw smooth rounded tooltip
        int bgAlpha = (int)(alpha * 0.95f * 255);
        int borderAlpha = (int)(alpha * 0.6f * 255);
        int bgColor = (bgAlpha << 24) | 0x121212;
        int borderColor = (borderAlpha << 24) | 0xFFFFFF;

        drawSmoothRoundedRect(context, tx, ty, totalWidth, totalHeight, 6.0f, bgColor);
        drawSmoothRoundedBorder(context, tx, ty, totalWidth, totalHeight, 6.0f, borderColor);

        // Draw text
        int currentY = ty + padding;
        for (String lineText : lines) {
            Text styledLine = Text.literal(lineText).setStyle(modernStyle);
            int textColor = ((int)(alpha * 255) << 24) | 0xE0E0E0;

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
    }

    private void updateScrolling(float delta, int textWidth, int availableWidth) {
        int maxScroll = textWidth - availableWidth;

        scrollTimer += delta;

        if (scrollTimer < PAUSE_DURATION) {
            return;
        }

        float scrollDelta = (SCROLL_SPEED * delta) / 20.0f;

        if (scrollDirection) {
            scrollOffset += scrollDelta;
            if (scrollOffset >= maxScroll) {
                scrollOffset = maxScroll;
                scrollDirection = false;
                scrollTimer = 0.0f;
            }
        } else {
            scrollOffset -= scrollDelta;
            if (scrollOffset <= 0) {
                scrollOffset = 0;
                scrollDirection = true;
                scrollTimer = 0.0f;
            }
        }
    }

    private void renderScrollingText(DrawContext context, MinecraftClient client, Text text,
                                     int x, int y, int w, int h, int color, int availableWidth) {
        context.enableScissor(x + TEXT_PADDING, y, x + w - TEXT_PADDING, y + h);

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
}