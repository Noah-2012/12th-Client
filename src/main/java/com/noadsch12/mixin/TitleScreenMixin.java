package com.noadsch12.mixin;

import com.noadsch12.ui.ClientSettingsScreen;
import com.noadsch12.ui.AnimatedButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import static com.noadsch12.BasicGlobals.getButtonMiddleX;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void addButton(CallbackInfo ci) {
        TitleScreen titleScreen = (TitleScreen) (Object) this;

        int screenWidth = titleScreen.width;
        int screenHeight = titleScreen.height;

        int buttonX = getButtonMiddleX(screenWidth, 200); // screenWidth / 2 - 100
        int buttonY = screenHeight / 4 + 48 - 24;

        //MenuButton clientButton = new MenuButton(
        //        "12th Client",
        //        buttonX,
        //        buttonY,
        //        200,
        //        20,
        //        btn -> onButtonPress(titleScreen)
        //);

        //clientButton.addToScreen(titleScreen);

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
        //MinecraftClient client = MinecraftClient.getInstance();
        //TextRenderer textRenderer = client.textRenderer;
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String msg1 = "12th Client";
        String msg2 = "by Noadsch12";

        int screenWidth = drawContext.getScaledWindowWidth();
        int x1 = screenWidth - textRenderer.getWidth(msg1) - 80;
        int y1 = 5;

        int x2 = screenWidth - textRenderer.getWidth(msg2) - 6;
        int y2 = 5;

        drawContext.drawTextWithShadow(
                textRenderer,
                msg1,
                x1,
                y1,
                0xFF00FFFF
        );

        drawContext.drawTextWithShadow(
                textRenderer,
                msg2,
                x2,
                y2,
                0xFFFFFFFF
        );
    }

    private void onButtonPress(TitleScreen titleScreen) {
        MinecraftClient.getInstance().setScreen(new ClientSettingsScreen(titleScreen));
    }
}