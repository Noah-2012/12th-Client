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

package com.noadsch12.ui.screens;

import com.noadsch12.BasicGlobals;
import com.noadsch12.ClientCrashHandler;
import com.noadsch12.TwelfthConfig;
import com.noadsch12.ui.BlurHandler;
import com.noadsch12.ui.widgets.ModernButton;
import com.noadsch12.util.AntiAFK;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
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
            x += (float) (dx * 0.05);
            y += (float) (dy * 0.05);
        }

        if (x < 0 || x > w) vx *= -1;
        if (y < 0 || y > h) vy *= -1;
    }
}

public class ClientSettingsScreen extends Screen {
    private final Screen parent;
    private final Text title;
    private final Style font = Style.EMPTY.withFont(new StyleSpriteSource.Font(BasicGlobals.ARIAL_FONT));
    public static boolean ProjectileDingEnabled = true;
    public static boolean jumpToFoodEnabled = true;
    public static boolean EntityCullingEnabled = true;
    public static boolean ProjectileTrailEnabled = true;
    public static boolean AutoTotemEnabled = true;
    public static boolean AutoArmorEnabled = true;
    public static boolean AutoRefillEnabled = true;
    public static boolean AutoToolEnabled = true;
    public static boolean BetterChatEnabled = true;
    public static boolean ItemDisplayEnabled = true;
    public static boolean BetterScoreboardEnabled = true;
    public static boolean HideTotemAnimEnabled = true;
    public static boolean SeeThroughGuiEnabled = true;
    public static boolean HideExplosionParticlesEnabled = true;
    public static boolean ShowKeystrokeSettingsEnabled = true;
    public static boolean NoDamageTiltEnabled = true;
    public static boolean AimbotEnabled = true;
    public static boolean EntityEspEnabled = true;
    public static boolean ChestESPEnabled = true;
    public static boolean CompassHudEnabled = true;
    public static boolean PlayerESPEnabled = true;
    public static boolean AntiWebEnabled = true;
    public static boolean AntiAFKEnabled = true;
    public static boolean NoSlowEnabled = true;
    public static boolean AntiKnockbackEnabled = true;
    public static boolean CriticalsEnabled = true;

    private static final String[] TRAIL_NAMES = {"Totem", "Explosion", "Hearts", "Line Trail"};
    private static final String[] TRAIL_COLORS = {"§cRed§r", "§9Blue§r", "§aGreen§r", "§fWhite§r", "§0Black§r"};
    public static int trailIndex = 0;
    public static int trailColorIndex = 0;

    private final List<Dot> dots = new ArrayList<>();

    public static void registerVars() {
        ProjectileDingEnabled = true;
        jumpToFoodEnabled = true;
        EntityCullingEnabled = true;
        ProjectileTrailEnabled = true;
        AutoTotemEnabled = true;
        AutoArmorEnabled = true;
        AutoRefillEnabled = true;
        AutoToolEnabled = true;
        BetterChatEnabled = true;
        ItemDisplayEnabled = true;
        BetterScoreboardEnabled = true;
        HideTotemAnimEnabled = true;
        SeeThroughGuiEnabled = true;
        ShowKeystrokeSettingsEnabled = true;
        NoDamageTiltEnabled = true;
        AimbotEnabled = true;
        EntityEspEnabled = true;
        ChestESPEnabled = true;
        CompassHudEnabled = true;
        PlayerESPEnabled = true;
        AntiWebEnabled = true;
        NoSlowEnabled = true;
        AntiKnockbackEnabled = true;
        CriticalsEnabled = true;

        trailIndex = 0;
        trailColorIndex = 0;
    }

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
        int centerY = (this.height / 2) - 50;
        int bWidth = 150;
        int bHeight = 20;

        int btcWidth = 150;
        int btcHeight = 10;

        int utilsX = centerX - 250;

        // Utils Bulk Buttons
        this.addDrawableChild(new ModernButton(utilsX, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§aEnable All"), btn -> {
            jumpToFoodEnabled = EntityCullingEnabled = BetterChatEnabled = ItemDisplayEnabled =
                    BetterScoreboardEnabled = ShowKeystrokeSettingsEnabled = AntiAFKEnabled = true;
            // Save all to config here if needed
            refreshUI();
        }));
        this.addDrawableChild(new ModernButton(utilsX + bWidth / 2 + 2, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§cDisable All"), btn -> {
            jumpToFoodEnabled = EntityCullingEnabled = BetterChatEnabled = ItemDisplayEnabled =
                    BetterScoreboardEnabled = ShowKeystrokeSettingsEnabled = AntiAFKEnabled = false;
            refreshUI();
        }));

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY - 20,
                bWidth,
                bHeight,
                Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    jumpToFoodEnabled = !jumpToFoodEnabled;
                    TwelfthConfig.setValue("jump_to_food_enabled", String.valueOf(jumpToFoodEnabled));
                    btn.setMessage(Text.literal("Jump to Food: " + (jumpToFoodEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Automatically jumps to next food item in hotbar"));

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 4,
                bWidth,
                bHeight,
                Text.literal("Entity Culling: " + (EntityCullingEnabled ? "§aOn by Default" : "§cOnly by Command")),
                btn -> {
                    EntityCullingEnabled = !EntityCullingEnabled;
                    TwelfthConfig.setValue("entity_culling_enabled", String.valueOf(EntityCullingEnabled));
                    btn.setMessage(Text.literal("Entity Culling: " + (EntityCullingEnabled ? "§aOn by Default" : "§cOnly by Command")));
                }).withTooltip("Ensures entities which cannot be seen aren't rendered"));

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 28,
                bWidth,
                bHeight,
                Text.literal("Better Chat: " + (BetterChatEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    BetterChatEnabled = !BetterChatEnabled;
                    TwelfthConfig.setValue("better_chat_enabled", String.valueOf(BetterChatEnabled));
                    btn.setMessage(Text.literal("Better Chat: " + (BetterChatEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Improves the Chat in many ways"));

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 52,
                bWidth,
                bHeight,
                Text.literal("Item Display: " + (ItemDisplayEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    ItemDisplayEnabled = !ItemDisplayEnabled;
                    TwelfthConfig.setValue("item_display_enabled", String.valueOf(ItemDisplayEnabled));
                    btn.setMessage(Text.literal("Item Display: " + (ItemDisplayEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Shows which and how many items are at one point"));

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 76,
                bWidth,
                bHeight,
                Text.literal("Better Scoreboard: " + (BetterScoreboardEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    BetterScoreboardEnabled = !BetterScoreboardEnabled;
                    TwelfthConfig.setValue("better_scoreboard_enabled", String.valueOf(BetterScoreboardEnabled));
                    btn.setMessage(Text.literal("Better Scoreboard: " + (BetterScoreboardEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Improves the Scoreboard with Design and Ordering"));

        ModernButton outline_settings = new ModernButton(
                utilsX,
                centerY + 148,
                bWidth,
                bHeight,
                Text.literal("Open Block Outline Settings"),
                btn -> {
                    this.client.setScreen(new BlockOutlineScreen(this));
                    btn.setMessage(Text.literal("Open Block Outline Settings"));
                });

        ModernButton keystroke_settings = new ModernButton(
                utilsX,
                centerY + 124,
                bWidth,
                bHeight,
                Text.literal("Open Keystroke Settings"),
                btn -> {
                    this.client.setScreen(new KeystrokesSettingsScreen(this));
                    btn.setMessage(Text.literal("Open Keystroke Settings"));
                });

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 100,
                bWidth,
                bHeight,
                Text.literal("Show Keystrokes: " + (ShowKeystrokeSettingsEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    ShowKeystrokeSettingsEnabled = !ShowKeystrokeSettingsEnabled;
                    this.remove(keystroke_settings);
                    keystroke_settings.active = ShowKeystrokeSettingsEnabled;
                    this.addDrawableChild(keystroke_settings);
                    TwelfthConfig.setValue("show_keystrokes_enabled", String.valueOf(ShowKeystrokeSettingsEnabled));
                    btn.setMessage(Text.literal("Show Keystrokes: " + (ShowKeystrokeSettingsEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Shows Keystrokes with Options like CPS, WASD and Mouse buttons"));

        this.addDrawableChild(keystroke_settings);

        this.addDrawableChild(outline_settings);

        this.addDrawableChild(new ModernButton(
                utilsX,
                centerY + 172,
                bWidth,
                bHeight,
                Text.literal("Anti AFK: " + (AntiAFKEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AntiAFKEnabled = !AntiAFKEnabled;
                    AntiAFK.enabled = AntiAFKEnabled;
                    TwelfthConfig.setValue("anti_afk_enabled", String.valueOf(AntiAFKEnabled));
                    btn.setMessage(Text.literal("Anti AFK: " + (AntiAFKEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Lets the Player jump every five Seconds"));

        int cheatsX = centerX + 100;

        // Cheats Bulk Buttons
        this.addDrawableChild(new ModernButton(cheatsX, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§aEnable All"), btn -> {
            AutoTotemEnabled = AutoArmorEnabled = AutoRefillEnabled = AutoToolEnabled =
                    NoDamageTiltEnabled = AimbotEnabled = ChestESPEnabled = PlayerESPEnabled =
                            AntiWebEnabled = NoSlowEnabled = AntiKnockbackEnabled = true;
            refreshUI();
        }));
        this.addDrawableChild(new ModernButton(cheatsX + bWidth / 2 + 2, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§cDisable All"), btn -> {
            AutoTotemEnabled = AutoArmorEnabled = AutoRefillEnabled = AutoToolEnabled =
                    NoDamageTiltEnabled = AimbotEnabled = ChestESPEnabled = PlayerESPEnabled =
                            AntiWebEnabled = NoSlowEnabled = AntiKnockbackEnabled = false;
            refreshUI();
        }));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY - 20,
                bWidth,
                bHeight,
                Text.literal("Auto Totem: " + (AutoTotemEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AutoTotemEnabled = !AutoTotemEnabled;
                    TwelfthConfig.setValue("auto_totem_enabled", String.valueOf(AutoTotemEnabled));
                    btn.setMessage(Text.literal("Auto Totem: " + (AutoTotemEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Automatically places the totem in the slot"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 4,
                bWidth,
                bHeight,
                Text.literal("Auto Armor: " + (AutoArmorEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AutoArmorEnabled = !AutoArmorEnabled;
                    TwelfthConfig.setValue("auto_armor_enabled", String.valueOf(AutoArmorEnabled));
                    btn.setMessage(Text.literal("Auto Armor: " + (AutoArmorEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Automatically places the armor in the right slots"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 28,
                bWidth,
                bHeight,
                Text.literal("Auto Refill: " + (AutoRefillEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AutoRefillEnabled = !AutoRefillEnabled;
                    TwelfthConfig.setValue("auto_refill_enabled", String.valueOf(AutoRefillEnabled));
                    btn.setMessage(Text.literal("Auto Refill: " + (AutoRefillEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Automatically refills an item in the hotbar"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 52,
                bWidth,
                bHeight,
                Text.literal("Auto Tool: " + (AutoToolEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AutoToolEnabled = !AutoToolEnabled;
                    TwelfthConfig.setValue("auto_tool_enabled", String.valueOf(AutoToolEnabled));
                    btn.setMessage(Text.literal("Auto Tool: " + (AutoToolEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Automatically jumps to the most efficient\ntool when performing an action"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 76,
                bWidth,
                bHeight,
                Text.literal("No Damage Tilt: " + (NoDamageTiltEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    NoDamageTiltEnabled = !NoDamageTiltEnabled;
                    TwelfthConfig.setValue("no_tilt_enabled", String.valueOf(NoDamageTiltEnabled));
                    btn.setMessage(Text.literal("No Damage Tilt: " + (NoDamageTiltEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Disables the Camera Tilt when getting damaged"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 100,
                bWidth,
                bHeight,
                Text.literal("Aimbot: " + (AimbotEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AimbotEnabled= !AimbotEnabled;
                    TwelfthConfig.setValue("aimbot_enabled", String.valueOf(AimbotEnabled));
                    btn.setMessage(Text.literal("Aimbot: " + (AimbotEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Always moves the crosshair to the nearest Player"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 124,
                bWidth,
                bHeight,
                Text.literal("Storage ESP: " + (ChestESPEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    ChestESPEnabled= !ChestESPEnabled;
                    TwelfthConfig.setValue("storage_esp_enabled", String.valueOf(ChestESPEnabled));
                    btn.setMessage(Text.literal("Storage ESP: " + (ChestESPEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Marks every Chest in ESP Radius and uses the CBCS\n(Chunk Block Cache System)"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 148,
                bWidth,
                bHeight,
                Text.literal("Player ESP: " + (PlayerESPEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    PlayerESPEnabled = !PlayerESPEnabled;
                    TwelfthConfig.setValue("player_esp_enabled", String.valueOf(PlayerESPEnabled));
                    btn.setMessage(Text.literal("Player ESP: " + (PlayerESPEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Marks every Player in ESP Radius and uses the PUCS\n(Player UUID Cache System)"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 172,
                bWidth,
                bHeight,
                Text.literal("Anti Web: " + (AntiWebEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AntiWebEnabled = !AntiWebEnabled;
                    TwelfthConfig.setValue("anti_web_enabled", String.valueOf(AntiWebEnabled));
                    btn.setMessage(Text.literal("Anti Web: " + (AntiWebEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Removes the slow down when walking into a Web"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 196,
                bWidth,
                bHeight,
                Text.literal("No Slow: " + (NoSlowEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    NoSlowEnabled = !NoSlowEnabled;
                    TwelfthConfig.setValue("no_slow_enabled", String.valueOf(NoSlowEnabled));
                    btn.setMessage(Text.literal("No Slow: " + (NoSlowEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Removes the slow down when using item like food\nor drawing a bow"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 220,
                bWidth,
                bHeight,
                Text.literal("Anti Knockback: " + (AntiKnockbackEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    AntiKnockbackEnabled = !AntiKnockbackEnabled;
                    TwelfthConfig.setValue("anti_knockback_enabled", String.valueOf(AntiKnockbackEnabled));
                    btn.setMessage(Text.literal("Anti Knockback: " + (AntiKnockbackEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Removes the knockback by canceling the packet"));

        this.addDrawableChild(new ModernButton(
                cheatsX,
                centerY + 244,
                bWidth,
                bHeight,
                Text.literal("Criticals: " + (CriticalsEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    CriticalsEnabled = !CriticalsEnabled;
                    TwelfthConfig.setValue("criticals_enabled", String.valueOf(CriticalsEnabled));
                    btn.setMessage(Text.literal("Criticals: " + (CriticalsEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Manipulates the Server packet so you do Criticals everytime"));

        int renderX = centerX - (bWidth / 2);

        // Rendering Bulk Buttons
        this.addDrawableChild(new ModernButton(renderX, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§aEnable All"), btn -> {
            HideTotemAnimEnabled = HideExplosionParticlesEnabled = EntityEspEnabled =
                    CompassHudEnabled = ProjectileDingEnabled = ProjectileTrailEnabled = true;
            refreshUI();
        }));
        this.addDrawableChild(new ModernButton(renderX + bWidth / 2 + 2, centerY - 38, bWidth / 2 - 2, 14, Text.literal("§cDisable All"), btn -> {
            HideTotemAnimEnabled = HideExplosionParticlesEnabled = EntityEspEnabled =
                    CompassHudEnabled = ProjectileDingEnabled = ProjectileTrailEnabled = false;
            refreshUI();
        }));

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY - 20,
                bWidth,
                bHeight,
                Text.literal("Hide Totem Animation: " + (HideTotemAnimEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    HideTotemAnimEnabled = !HideTotemAnimEnabled;
                    TwelfthConfig.setValue("hide_totem_enabled", String.valueOf(HideTotemAnimEnabled));
                    btn.setMessage(Text.literal("Hide Totem Animation: " + (HideTotemAnimEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Hides the Totem popping Animation so it does\nnot distract the Player"));

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY + 4,
                bWidth,
                bHeight,
                Text.literal("Hide Explosion Particles: " + (HideExplosionParticlesEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    HideExplosionParticlesEnabled = !HideExplosionParticlesEnabled;
                    TwelfthConfig.setValue("hide_explosion_enabled", String.valueOf(HideExplosionParticlesEnabled));
                    btn.setMessage(Text.literal("Hide Explosion Particles: " + (HideExplosionParticlesEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Hides the Explosion Particles so it does\nnot distract the Player"));

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY + 28,
                bWidth,
                bHeight,
                Text.literal("Entity ESP: " + (EntityEspEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    EntityEspEnabled = !EntityEspEnabled;
                    TwelfthConfig.setValue("entity_esp_enabled", String.valueOf(EntityEspEnabled));
                    btn.setMessage(Text.literal("Entity ESP: " + (EntityEspEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Makes Lines around Hitboxes from Entities\n(Green: peacefully, Yellow: not peacefully, Red: Player)"));

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY + 52,
                bWidth,
                bHeight,
                Text.literal("Compass HUD: " + (CompassHudEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    CompassHudEnabled = !CompassHudEnabled;
                    TwelfthConfig.setValue("compass_hud_enabled", String.valueOf(CompassHudEnabled));
                    btn.setMessage(Text.literal("Compass HUD: " + (CompassHudEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Makes a Compass in the top middle Screen that shows the Directions and Waypoints"));

        ModernButton trailColorButton = new ModernButton(
                renderX,
                centerY + 144,
                btcWidth,
                btcHeight,
                Text.literal("Trail Color: " + TRAIL_COLORS[trailColorIndex]),
                btn -> {
                    trailColorIndex = (trailColorIndex + 1) % TRAIL_COLORS.length;
                    TwelfthConfig.setValue("trail_color_index", String.valueOf(trailColorIndex));
                    btn.setMessage(Text.literal("Trail Color: " + TRAIL_COLORS[trailColorIndex]));
                }).withTooltip("Select a color for the trail");

        if (trailIndex != 3) {
            trailColorButton.active = false;
        }
        this.addDrawableChild(trailColorButton);

        ModernButton trailButton = new ModernButton(
                renderX,
                centerY + 124,
                bWidth,
                bHeight,
                Text.literal("Trail Mode: " + TRAIL_NAMES[trailIndex]),
                btn -> {
                    this.remove(trailColorButton);
                    trailIndex = (trailIndex + 1) % TRAIL_NAMES.length;
                    if (trailIndex == 3) {
                        trailColorButton.active = true;
                    } else {
                        trailColorButton.active = false;
                    }
                    TwelfthConfig.setValue("trail_index", String.valueOf(trailIndex));
                    this.addDrawableChild(trailColorButton);
                    btn.setMessage(Text.literal("Trail Mode: " + TRAIL_NAMES[trailIndex]));
                }).withTooltip("Select a trail mode");

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY + 76,
                bWidth,
                bHeight,
                Text.literal("Projectile Ding: " + (ProjectileDingEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    ProjectileDingEnabled = !ProjectileDingEnabled;
                    TwelfthConfig.setValue("projectile_ding_enabled", String.valueOf(ProjectileDingEnabled));
                    btn.setMessage(Text.literal("Projectile Ding: " + (ProjectileDingEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Makes a ding when hitting an entity\n(high when not killed; low when killed)"));

        this.addDrawableChild(new ModernButton(
                renderX,
                centerY + 100,
                bWidth,
                bHeight,
                Text.literal("Projectile Trail: " + (ProjectileTrailEnabled ? "§aON" : "§cOFF")),
                btn -> {
                    ProjectileTrailEnabled = !ProjectileTrailEnabled;
                    this.remove(trailButton);
                    trailButton.active = ProjectileTrailEnabled;
                    this.addDrawableChild(trailButton);
                    if (trailIndex == 3) {
                        this.remove(trailColorButton);
                        trailColorButton.active = ProjectileTrailEnabled;
                        this.addDrawableChild(trailColorButton);
                    }
                    TwelfthConfig.setValue("projectile_trail_enabled", String.valueOf(ProjectileTrailEnabled));
                    btn.setMessage(Text.literal("Projectile Trail: " + (ProjectileTrailEnabled ? "§aON" : "§cOFF")));
                }).withTooltip("Makes a trail behind arrows"));

        if (!ProjectileTrailEnabled) {
            trailButton.active = false;
        }
        this.addDrawableChild(trailButton);

        this.shouldCloseOnEsc();

        // === DEV / DEBUG ===
        this.addDrawableChild(new ModernButton(
                centerX - 50,
                centerY + 300,
                100,
                20,
                Text.literal("§cCrash Client"),
                btn -> {
                    ClientCrashHandler.handleCrash(null);
                }
        ).withTooltip("Intentionally crashes the client\n(for testing the crash handler)"));


        if (this.client.world == null) {
            this.addDrawableChild(new ModernButton(
                    centerX - 50,
                    centerY + 275,
                    100,
                    20,
                    Text.literal("Back"),
                    btn -> this.close()));
        }
    }

    private void stacko() {
        stacko();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client.world != null) {
            BlurHandler.executeBlur(context, this.width, this.height, 2.0f);
            super.render(context, mouseX, mouseY, deltaTicks);
        } else {
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
        int centerY = (this.height / 2) - 62;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFFFF);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Utils").setStyle(font), centerX - 180, centerY - 40, 0xFFFFAA00);
        context.drawItem(new ItemStack(Items.COMPASS), centerX - 205, centerY - 45);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Rendering & Accessories").setStyle(font), centerX, centerY - 40, 0xFFFFAA00);
        context.drawItem(new ItemStack(Items.SPYGLASS), centerX - 6, centerY - 62);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Cheats").setStyle(font), centerX + 158, centerY - 40, 0xFFFFAA00);
        context.drawItem(new ItemStack(Items.BARRIER), centerX + 193, centerY - 45);

        int previewX = centerX + 83;
        int previewY = centerY + 78;

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

    private void refreshUI() {
        this.clearAndInit();
    }
}