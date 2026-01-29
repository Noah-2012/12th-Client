package com.noadsch12.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

public class SmallAsciiRenderer {

    // Identifier for the monospace font
    private static final Identifier MONO_ID = Identifier.of("minecraft", "uniform");

    public static void drawAsciiArt(DrawContext context, TextRenderer renderer, String[] lines, float x, float y, float scale, int color) {
        var matrices = context.getMatrices();

        matrices.pushMatrix(); // As requested

        // Positioning and Scaling
        matrices.translate(x, y);
        matrices.scale(scale, scale);

        // Applying the specific StyleSpriteSource.Font syntax you provided
        Style monoStyle = Style.EMPTY.withFont(new StyleSpriteSource.Font(MONO_ID));

        float lineOffset = 0;
        float lineSpacing = 9.0f; // Tighter spacing for ASCII

        for (String line : lines) {

            Text formattedLine = Text.literal(line).setStyle(monoStyle);

            context.drawText(renderer, formattedLine, 0, (int)lineOffset, color, false);
            lineOffset += lineSpacing;
        }

        matrices.popMatrix(); // As requested
    }
}