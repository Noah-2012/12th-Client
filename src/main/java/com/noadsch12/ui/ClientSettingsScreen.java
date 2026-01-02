package com.noadsch12.ui;

import io.wispforest.owo.ui.core.ParentComponent;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

class Dot {
    float x, y, vx, vy;
    Dot(int w, int h) {
        x = (float)Math.random() * w;
        y = (float)Math.random() * h;
        vx = ((float)Math.random() - 0.5f) * 0.5f;
        vy = ((float)Math.random() - 0.5f) * 0.5f;
    }
    void update(int w, int h, double mouseX, double mouseY) {
        x += vx; y += vy;

        double dx = x - mouseX;
        double dy = y - mouseY;
        double dist = Math.sqrt(dx*dx + dy*dy);

        if (dist < 40) {
            x += dx * 0.05;
            y += dy * 0.05;
        }

        if (x < 0 || x > w) vx *= -1;
        if (y < 0 || y > h) vy *= -1;
    }
}

public class ClientSettingsScreen extends Screen {
    private final Screen parent;
    private final Text title;
    public static boolean ProjectileDingEnabled = true;
    public static boolean jumpToFoodEnabled = true;
    public static boolean EntityCullingEnabled = true;
    public static boolean ProjectileTrailEnabled = true;
    public static boolean AutoTotemEnabled = true;
    public static boolean AutoArmorEnabled = true;
    public static boolean AutoRefillEnabled = true;
    public static boolean AutoToolEnabled = true;
    private static final String[] TRAIL_NAMES = {"Totem", "Explosion", "Hearts", "Line Trail"};
    private static final String[] TRAIL_COLORS = {"Red", "Blue", "Green", "White", "Black"};
    public static int trailIndex = 0;
    public static int trailColorIndex = 0;

    private final List<Dot> dots = new ArrayList<>();

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        this.title = Text.literal("12th Client Settings");
    }

    @Override
    protected void init() {
        if(dots.isEmpty()) {
            for(int i = 0; i < 90; i++) dots.add(new Dot(this.width, this.height));
        }

        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int bWidth = 150;
        int bHeight = 20;

        int btcWidth = 150;
        int btcHeight = 10;

        int utilsX = centerX - 250;

        // Jump to Food Toggle Button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            jumpToFoodEnabled = !jumpToFoodEnabled;
                            btn.setMessage(Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(utilsX, centerY - 20)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Entity Culling: " + (EntityCullingEnabled ? "§aOn by Default" : "§cOnly by Command")),
                        btn -> {
                            EntityCullingEnabled =! EntityCullingEnabled;
                            btn.setMessage(Text.literal("Entity Culling: " + (EntityCullingEnabled ? "§aOn by Default" : "§cOnly by Command")));
                        }
                )
                .position(utilsX, centerY + 4)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Auto Totem: " + (AutoTotemEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            AutoTotemEnabled =! AutoTotemEnabled;
                            btn.setMessage(Text.literal("Auto Totem: " + (AutoTotemEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(utilsX, centerY + 28)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Auto Armor: " + (AutoArmorEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            AutoArmorEnabled =! AutoArmorEnabled;
                            btn.setMessage(Text.literal("Auto Armor: " + (AutoArmorEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(utilsX, centerY + 52)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Auto Refill: " + (AutoRefillEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            AutoRefillEnabled =! AutoRefillEnabled;
                            btn.setMessage(Text.literal("Auto Refill: " + (AutoRefillEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(utilsX, centerY + 76)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Auto Tool: " + (AutoToolEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            AutoToolEnabled =! AutoToolEnabled;
                            btn.setMessage(Text.literal("Auto Tool: " + (AutoToolEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(utilsX, centerY + 100)
                .size(bWidth, bHeight)
                .build());

        int renderX = centerX - (bWidth / 2);

        ButtonWidget trailColorButton = ButtonWidget.builder(
                        Text.literal("Trail Color: " + TRAIL_COLORS[trailColorIndex]),
                        btn -> {
                            trailColorIndex = (trailColorIndex + 1) % TRAIL_COLORS.length;
                            btn.setMessage(Text.literal("Trail Color: " + TRAIL_COLORS[trailColorIndex]));
                        }
                )
                .position(renderX, centerY + 48)
                .size(btcWidth, btcHeight)
                .build();

        if (trailIndex != 3) {
            trailColorButton.active = false;
        }
        this.addDrawableChild(trailColorButton);

        ButtonWidget trailButton = ButtonWidget.builder(
                        Text.literal("Trail Mode: " + TRAIL_NAMES[trailIndex]),
                        btn -> {
                            this.remove(trailColorButton);
                            trailIndex = (trailIndex + 1) % TRAIL_NAMES.length;
                            if (trailIndex == 3) {
                                trailColorButton.active = true;
                            } else {
                                trailColorButton.active = false;
                            }
                            this.addDrawableChild(trailColorButton);
                            btn.setMessage(Text.literal("Trail Mode: " + TRAIL_NAMES[trailIndex]));
                        }
                )
                .position(renderX, centerY + 28)
                .size(bWidth, bHeight)
                .build();

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Projectile Ding: " + (ProjectileDingEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            ProjectileDingEnabled = !ProjectileDingEnabled;
                            btn.setMessage(Text.literal("Projectile Ding: " + (ProjectileDingEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(renderX, centerY - 20)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Projectile Trail: " + (ProjectileTrailEnabled ? "§aON" : "§cOFF")),
                        btn -> {
                            ProjectileTrailEnabled = !ProjectileTrailEnabled;
                            this.remove(trailButton);
                            trailButton.active = ProjectileTrailEnabled;
                            this.addDrawableChild(trailButton);
                            btn.setMessage(Text.literal("Projectile Trail: " + (ProjectileTrailEnabled ? "§aON" : "§cOFF")));
                        }
                )
                .position(renderX, centerY + 4)
                .size(bWidth, bHeight)
                .build());

        this.addDrawableChild(trailButton);

        this.shouldCloseOnEsc();

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        btn -> this.close()
                )
                .position(centerX - 50, this.height - 40)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client.world != null) {
            // --- IN-GAME HUD MODE ---
            // This renders the blurred world background when opened via Keybind
            //context.applyBlur();
            //context.fill(0, 0, this.width, this.height, 0x90101010);
            BlurHandler.executeBlur(context, this.width, this.height, 5.0f);
            super.render(context, mouseX, mouseY, deltaTicks);
        } else {
            // --- MAIN MENU MODE ---
            // Your custom Particle/Dot background logic
            context.fill(0, 0, this.width, this.height, 0xFF101010);

            for (Dot dot : dots) {
                dot.update(this.width, this.height, mouseX, mouseY);
                context.fill((int)dot.x, (int)dot.y, (int)dot.x + 2, (int)dot.y + 2, 0x55FFFFFF);
            }

            for (int i = 0; i < dots.size(); i++) {
                Dot a = dots.get(i);
                for (int j = i + 1; j < dots.size(); j++) {
                    Dot b = dots.get(j);
                    double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));

                    if (dist < 25) {
                        int alpha = (int) ((1.0 - (dist / 50.0)) * 100);
                        int color = (alpha << 24) | 0xFFFFFF;
                        context.fill((int)a.x, (int)a.y, (int)b.x, (int)b.y + 1, color);
                    }
                }
            }

            super.render(context, mouseX, mouseY, deltaTicks);
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Optional: Titel anzeigen
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFFFF);

        context.drawTextWithShadow(this.textRenderer, "Utils", centerX - 180, centerY - 40, 0xFFFFAA00);
        context.drawItem(new ItemStack(Items.COMPASS), centerX - 205, centerY - 45);

        context.drawCenteredTextWithShadow(this.textRenderer, "Rendering & Accessories", centerX, centerY - 40, 0xFFFFAA00);
        context.drawItem(new ItemStack(Items.SPYGLASS), centerX - 6, centerY - 62);

        int previewX = centerX + 85;
        int previewY = centerY + 30;

        ItemStack previewStack = switch (trailIndex) {
            case 0 -> new ItemStack(Items.TOTEM_OF_UNDYING);
            case 1 -> new ItemStack(Items.TNT);
            case 3 -> new ItemStack(Items.STICK);
            default -> ItemStack.EMPTY;
        };

        if (!previewStack.isEmpty()) {
            context.drawItem(previewStack, previewX, previewY);
        } else if (trailIndex == 2) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, Identifier.ofVanilla("hud/heart/full"), previewX, previewY + 2, 11, 11, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}