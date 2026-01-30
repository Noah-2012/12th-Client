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

import com.noadsch12.ui.screens.ClientSettingsScreen;
import com.noadsch12.ui.screens.ClientInfoScreen;
import com.noadsch12.ui.widgets.AnimatedButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.MinecraftClient;
import static com.noadsch12.BasicGlobals.getButtonMiddleX;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static final Identifier CLIENT_LOGO = Identifier.of("12th-client", "logo2.png");

    @Unique
    private int logoX1, logoY1, logoX2, logoY2;
    @Unique
    private int textX1, textY1, textWidth1, textHeight1;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addButton(CallbackInfo ci) {
        TitleScreen titleScreen = (TitleScreen) (Object) this;

        int screenWidth = titleScreen.width;
        int screenHeight = titleScreen.height;

        int buttonX = getButtonMiddleX(screenWidth, 200);
        int buttonY = screenHeight / 4 + 48 - 24;

        AnimatedButtonWidget clientButton = new AnimatedButtonWidget(
                buttonX,
                buttonY,
                200,
                20,
                Text.literal("12th Client"),
                btn -> onButtonPress(titleScreen),
                "12th Client"
        );

        titleScreen.addDrawableChild(clientButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderClientBrand(DrawContext drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String msg1 = "12th Client";
        String msg2 = "by Noadsch12";

        String crd_msg1 = "Credits to";
        String crd_msg2 = "SniperShot";
        String crd_msg3 = "for promoting";

        int screenWidth = drawContext.getScaledWindowWidth();

        // Logo-Dimensionen
        int logoWidth = 128;
        int logoHeight = 128;
        int logoPadding = 5;

        logoX1 = screenWidth - logoWidth - logoPadding;
        logoY1 = 27;
        logoX2 = logoX1 + logoWidth;
        logoY2 = logoY1 + logoHeight;

        // Check if mouse is hovering over logo
        boolean isHoveringLogo = mouseX >= logoX1 && mouseX <= logoX2 && mouseY >= logoY1 && mouseY <= logoY2;

        // Draw glow effect when hovering
        if (isHoveringLogo) {
            //drawContext.fillGradient(logoX1 - 2, logoY1 - 2, logoX2 + 2, logoY2 + 2, 0xFF000000, 0xFF808080);
            drawContext.fill(logoX1 + 5, logoY1 + 5, logoX2 - 5, logoY2 - 5, 0x43808080);
        }

        drawContext.drawTexturedQuad(
                CLIENT_LOGO,
                logoX1,
                logoY1,
                logoX2,
                logoY2,
                0.0f,
                1.0f,
                0.0f,
                1.0f
        );

        int x1 = screenWidth - textRenderer.getWidth(msg1) - 80;
        int y1 = 5;

        // Store text bounds for click detection
        textX1 = x1;
        textY1 = y1;
        textWidth1 = textRenderer.getWidth(msg1);
        textHeight1 = textRenderer.fontHeight;

        // Check if mouse is hovering over text
        boolean isHoveringText = mouseX >= textX1 && mouseX <= textX1 + textWidth1 &&
                mouseY >= textY1 && mouseY <= textY1 + textHeight1;

        int x2 = screenWidth - textRenderer.getWidth(msg2) - 6;
        int y2 = 5;

        int x3 = screenWidth - textRenderer.getWidth(crd_msg1) - 135;
        int y3 = 15;

        int x4 = screenWidth - textRenderer.getWidth(crd_msg2) - 78;
        int y4 = 15;

        int x5 = screenWidth - textRenderer.getWidth(crd_msg3) - 6;
        int y5 = 15;

        // Change color when hovering
        int textColor = isHoveringText ? 0xFF00FFAA : 0xFF00FFFF;

        drawContext.drawTextWithShadow(
                textRenderer,
                msg1,
                x1,
                y1,
                textColor
        );

        // Underline when hovering
        if (isHoveringText) {
            drawContext.fill(x1, y1 + textHeight1, x1 + textWidth1, y1 + textHeight1 + 1, 0xFF00FFFF);
        }

        drawContext.drawTextWithShadow(
                textRenderer,
                msg2,
                x2,
                y2,
                0xFFFFFFFF
        );

        drawContext.drawTextWithShadow(
                textRenderer,
                crd_msg1,
                x3,
                y3,
                0xFFFFFFFF
        );

        drawContext.drawText(
                textRenderer,
                crd_msg2,
                x4,
                y4,
                0xFF00008B,
                false
        );

        drawContext.drawTextWithShadow(
                textRenderer,
                crd_msg3,
                x5,
                y5,
                0xFFFFFFFF
        );
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0) { // Left click
            // Check if clicked on logo
            if (mouseX >= logoX1 && mouseX <= logoX2 && mouseY >= logoY1 && mouseY <= logoY2) {
                TitleScreen titleScreen = (TitleScreen) (Object) this;
                MinecraftClient.getInstance().setScreen(new ClientInfoScreen(titleScreen));
                cir.setReturnValue(true);
                return;
            }

            // Check if clicked on text
            if (mouseX >= textX1 && mouseX <= textX1 + textWidth1 &&
                    mouseY >= textY1 && mouseY <= textY1 + textHeight1) {
                TitleScreen titleScreen = (TitleScreen) (Object) this;
                MinecraftClient.getInstance().setScreen(new ClientInfoScreen(titleScreen));
                cir.setReturnValue(true);
            }
        }
    }

    private void onButtonPress(TitleScreen titleScreen) {
        MinecraftClient.getInstance().setScreen(new ClientSettingsScreen(titleScreen));
    }
}