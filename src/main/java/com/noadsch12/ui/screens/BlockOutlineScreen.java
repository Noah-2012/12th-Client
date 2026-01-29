package com.noadsch12.ui.screens;

import com.noadsch12.ui.BlockOutlineSettings;
import com.noadsch12.TwelfthConfig;
import com.noadsch12.ui.BlurHandler;
import com.noadsch12.ui.widgets.ModernButton;
import com.noadsch12.ui.widgets.ModernSlider; // Import the new slider
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BlockOutlineScreen extends Screen {
    private final Screen parent;

    public BlockOutlineScreen(Screen parent) {
        super(Text.literal("Outline Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int bWidth = 160;

        // Toggle Outline
        this.addDrawableChild(new ModernButton(centerX - 80, centerY - 60, bWidth, 20,
                Text.literal("Custom Outline: " + (BlockOutlineSettings.enabled ? "§aON" : "§cOFF")),
                btn -> {
                    BlockOutlineSettings.enabled = !BlockOutlineSettings.enabled;
                    TwelfthConfig.setValue("outline_enabled", String.valueOf(BlockOutlineSettings.enabled));
                    btn.setMessage(Text.literal("Custom Outline: " + (BlockOutlineSettings.enabled ? "§aON" : "§cOFF")));
                }));

        // Rainbow Toggle
        this.addDrawableChild(new ModernButton(centerX - 80, centerY - 35, bWidth, 20,
                Text.literal("Rainbow: " + (BlockOutlineSettings.rainbow ? "§aON" : "§cOFF")),
                btn -> {
                    BlockOutlineSettings.rainbow = !BlockOutlineSettings.rainbow;
                    btn.setMessage(Text.literal("Rainbow: " + (BlockOutlineSettings.rainbow ? "§aON" : "§cOFF")));
                }));

        // Red Slider
        this.addDrawableChild(new ModernSlider(centerX - 80, centerY - 10, bWidth, 20,
                Text.literal("Red §c"), BlockOutlineSettings.r,
                val -> BlockOutlineSettings.r = val.floatValue())
                .withTooltip("Adjust the Red component"));

        // Green Slider
        this.addDrawableChild(new ModernSlider(centerX - 80, centerY + 15, bWidth, 20,
                Text.literal("Green §a"), BlockOutlineSettings.g,
                val -> BlockOutlineSettings.g = val.floatValue())
                .withTooltip("Adjust the Green component"));

        // Blue Slider
        this.addDrawableChild(new ModernSlider(centerX - 80, centerY + 40, bWidth, 20,
                Text.literal("Blue §9"), BlockOutlineSettings.b,
                val -> BlockOutlineSettings.b = val.floatValue())
                .withTooltip("Adjust the Blue component"));

        // Opacity Slider
        this.addDrawableChild(new ModernSlider(centerX - 80, centerY + 65, bWidth, 20,
                Text.literal("Opacity §f"), BlockOutlineSettings.a,
                val -> BlockOutlineSettings.a = val.floatValue())
                .withTooltip("Adjust how transparent the outline is"));

        // Back Button
        this.addDrawableChild(new ModernButton(centerX - 50, centerY + 100, 100, 20, Text.literal("Back"), btn -> this.close()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client.world != null) {
            BlurHandler.executeBlur(context, this.width, this.height, 2.0f);
        } else {
            context.fill(0, 0, this.width, this.height, 0xFF101010);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Block Outline Customization"), this.width / 2, 20, 0xFFFFAA00);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        // Save all settings when the screen is closed to keep config updated
        //TwelfthConfig.setValue("outline_r", String.valueOf(BlockOutlineSettings.r));
        //TwelfthConfig.setValue("outline_g", String.valueOf(BlockOutlineSettings.g));
        //TwelfthConfig.setValue("outline_b", String.valueOf(BlockOutlineSettings.b));
        //TwelfthConfig.setValue("outline_a", String.valueOf(BlockOutlineSettings.a));

        this.client.setScreen(parent);
    }
}