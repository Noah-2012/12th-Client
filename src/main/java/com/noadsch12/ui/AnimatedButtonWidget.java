package com.noadsch12.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnimatedButtonWidget extends ButtonWidget {
    private final String baseText;
    private long lastUpdate = System.currentTimeMillis();
    private float colorPhase = 0f;

    public AnimatedButtonWidget(int x, int y, int width, int height,
                                Text message, PressAction onPress, String baseText) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.baseText = baseText;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdate;
        lastUpdate = currentTime;

        colorPhase += elapsed * 0.006f;
        updateColorAnimation();

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    private void updateColorAnimation() {
        // Schwarz-Weiß Pulsieren mit Standard-Farbcodes
        float brightness = (float) (Math.sin(colorPhase) * 0.5 + 0.5);
        String afterCode;
        afterCode = "§kniM";

        String colorCode;
        if (brightness < 0.25f) {
            colorCode = "§0§kMin§r§0"; // Black
        } else if (brightness < 0.5f) {
            colorCode = "§8§kMin§r§8"; // Dark Grey
        } else if (brightness < 0.75) {
            colorCode = "§7§kMin§r§7"; // Grey
        } else {
            colorCode = "§f§kMin§r§f"; // White
        }

        this.setMessage(Text.literal(colorCode + baseText + afterCode));
    }
}