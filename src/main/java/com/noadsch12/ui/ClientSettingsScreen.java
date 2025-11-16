package com.noadsch12.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ClientSettingsScreen extends Screen {
    private final Screen parent;
    private final Text title;
    public static boolean ChatLoggerEnabled = true;
    public static boolean jumpToFoodEnabled = true;

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        this.title = Text.literal("12th Client Settings");
    }

    @Override
    protected void init() {
        super.init();

        // Jump to Food Toggle Button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            jumpToFoodEnabled = !jumpToFoodEnabled;
                            btn.setMessage(Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(this.width / 2 - 75, this.height / 2 - 20)
                .size(150, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Chatlog System: " + (ChatLoggerEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            ChatLoggerEnabled = !ChatLoggerEnabled;
                            btn.setMessage(Text.literal("Chatlog System: " + (ChatLoggerEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(this.width / 2 - 75, this.height / 2 + 4)
                .size(150, 20)
                .build());

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        btn -> this.close()
                )
                .position(this.width / 2 - 50, this.height / 2 + 230)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // #000000 → #434343
        context.fillGradient(0, 0, this.width, this.height, 0xFF000000, 0xFF434343);
        // Hintergrund rendern (dunkler Standardhintergrund)
        super.render(context, mouseX, mouseY, deltaTicks);

        // Optional: Titel anzeigen
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}