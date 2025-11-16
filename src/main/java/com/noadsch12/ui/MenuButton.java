package com.noadsch12.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuButton {
    private final ButtonWidget button;
    public MenuButton(String label, int x, int y, int width, int height, ButtonWidget.PressAction onPress) {
        this.button = ButtonWidget.builder(
                        Text.literal(label),
                        onPress
                )
                .position(x, y)
                .size(width, height)
                .build();
    }

    public void addToScreen(Screen screen) {
        screen.addDrawableChild(this.button);
    }

    public ButtonWidget getButton() {
        return this.button;
    }
}