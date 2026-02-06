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
import com.noadsch12.modules.Category;
import com.noadsch12.modules.Module;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.modules.impl.misc.ShowKeystrokesModule;
import com.noadsch12.modules.impl.render.ProjectileTrailModule;
import com.noadsch12.modules.impl.render.TrailSettings;
import com.noadsch12.ui.BlurHandler;
import com.noadsch12.ui.widgets.ModernButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

class Dot {
    float x, y;
    float vx, vy;
    float shakeX = 0;
    float shakeY = 0;

    Dot(int w, int h) {
        x = (float) Math.random() * w;
        y = (float) Math.random() * h;
        vx = ((float) Math.random() - 0.5f) * 0.5f;
        vy = ((float) Math.random() - 0.5f) * 0.5f;
    }

    void applyShake() {
        shakeX += ((float) Math.random() - 0.5f) * 6.5f;
        shakeY += ((float) Math.random() - 0.5f) * 6.5f;
    }

    void update(int w, int h, double mouseX, double mouseY) {
        x += vx + shakeX;
        y += vy + shakeY;

        shakeX *= 0.85f;
        shakeY *= 0.85f;

        double dx = x - mouseX;
        double dy = y - mouseY;
        double dist = Math.sqrt(dx * dx + dy * dy);

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
    private final List<Dot> dots = new ArrayList<>();
    private final ModuleManager moduleManager = ModuleManager.getInstance();

    // UI Constants
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TRAIL_BUTTON_HEIGHT = 10;
    private static final int BUTTON_SPACING = 24;

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        this.title = Text.literal("12th Client Settings");
    }

    @Override
    protected void init() {
        if (dots.isEmpty()) {
            for (int i = 0; i < 90; i++) {
                dots.add(new Dot(this.width, this.height));
            }
        }

        super.init();

        int centerX = this.width / 2;
        int centerY = (this.height / 2) - 50;

        // Create buttons for each category
        for (Category category : Category.values()) {
            createCategoryButtons(category, centerX, centerY);
        }

        // Special buttons (settings screens, crash test, back)
        createSpecialButtons(centerX, centerY);

        this.shouldCloseOnEsc();
    }

    /**
     * Create all buttons for a specific category
     */
    private void createCategoryButtons(Category category, int centerX, int centerY) {
        int columnX = category.getColumnX(centerX, BUTTON_WIDTH);
        int currentY = centerY - 20;

        // Bulk enable/disable buttons
        addBulkButtons(category, columnX, centerY - 38);

        // Get all modules for this category
        List<Module> modules = moduleManager.getModulesByCategory(category);

        for (Module module : modules) {
            ModernButton button = new ModernButton(
                    columnX,
                    currentY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    module.getButtonLabel(),
                    btn -> {
                        module.toggle();
                        btn.setMessage(module.getButtonLabel());

                        // Handle special module logic
                        handleSpecialModuleToggle(module);
                    }
            ).withTooltip(module.getTooltip());

            this.addDrawableChild(button);
            currentY += BUTTON_SPACING;
        }

        // Add category-specific special buttons
        if (category == Category.MISC) {
            addMiscSpecialButtons(columnX, currentY);
        } else if (category == Category.RENDER) {
            addRenderSpecialButtons(columnX, currentY);
        }
    }

    /**
     * Add bulk enable/disable buttons for a category
     */
    private void addBulkButtons(Category category, int x, int y) {
        this.addDrawableChild(new ModernButton(
                x,
                y,
                BUTTON_WIDTH / 2 - 2,
                14,
                Text.literal("§aEnable All"),
                btn -> {
                    moduleManager.enableCategory(category);
                    refreshUI();
                }
        ));

        this.addDrawableChild(new ModernButton(
                x + BUTTON_WIDTH / 2 + 2,
                y,
                BUTTON_WIDTH / 2 - 2,
                14,
                Text.literal("§cDisable All"),
                btn -> {
                    moduleManager.disableCategory(category);
                    refreshUI();
                }
        ));
    }

    /**
     * Add special buttons for Misc category
     */
    private void addMiscSpecialButtons(int x, int y) {
        ShowKeystrokesModule keystrokesModule = moduleManager.getModule(ShowKeystrokesModule.class);

        ModernButton keystrokeSettings = new ModernButton(
                x,
                y,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal("Open Keystroke Settings"),
                btn -> this.client.setScreen(new KeystrokesSettingsScreen(this))
        );
        keystrokeSettings.active = keystrokesModule != null && keystrokesModule.isEnabled();
        this.addDrawableChild(keystrokeSettings);

        this.addDrawableChild(new ModernButton(
                x,
                y + BUTTON_SPACING,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal("Open Block Outline Settings"),
                btn -> this.client.setScreen(new BlockOutlineScreen(this))
        ));
    }

    /**
     * Add special buttons for Render category (trail settings)
     */
    private void addRenderSpecialButtons(int x, int y) {
        ProjectileTrailModule trailModule = moduleManager.getModule(ProjectileTrailModule.class);
        boolean trailEnabled = trailModule != null && trailModule.isEnabled();

        // Trail color button
        ModernButton trailColorButton = new ModernButton(
                x,
                y + 20,
                BUTTON_WIDTH,
                TRAIL_BUTTON_HEIGHT,
                Text.literal("Trail Color: " + TrailSettings.getCurrentTrailColor()),
                btn -> {
                    TrailSettings.cycleTrailColorIndex();
                    btn.setMessage(Text.literal("Trail Color: " + TrailSettings.getCurrentTrailColor()));
                }
        ).withTooltip("Select a color for the trail");
        trailColorButton.active = trailEnabled && TrailSettings.isLineTrail();
        this.addDrawableChild(trailColorButton);

        // Trail mode button
        ModernButton trailButton = new ModernButton(
                x,
                y,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal("Trail Mode: " + TrailSettings.getCurrentTrailName()),
                btn -> {
                    this.remove(trailColorButton);
                    TrailSettings.cycleTrailIndex();
                    trailColorButton.active = trailEnabled && TrailSettings.isLineTrail();
                    this.addDrawableChild(trailColorButton);
                    btn.setMessage(Text.literal("Trail Mode: " + TrailSettings.getCurrentTrailName()));
                }
        ).withTooltip("Select a trail mode");
        trailButton.active = trailEnabled;
        this.addDrawableChild(trailButton);
    }

    /**
     * Add special buttons (crash test, back button)
     */
    private void createSpecialButtons(int centerX, int centerY) {
        // Developer crash test button
        this.addDrawableChild(new ModernButton(
                centerX - 50,
                centerY + 297,
                100,
                20,
                Text.literal("§cCrash Client"),
                btn -> {
                    throw new RuntimeException("12th Client managed Crash - THIS IS NOT A REAL CRASH - ");
                }
        ).withTooltip("Intentionally crashes the client\n(for testing the crash handler)"));

        // Back button (only show when not in-game)
        if (this.client.world == null) {
            this.addDrawableChild(new ModernButton(
                    centerX - 50,
                    centerY + 273,
                    100,
                    20,
                    Text.literal("Back"),
                    btn -> this.close()
            ));
        }
    }

    /**
     * Handle special logic when specific modules are toggled
     */
    private void handleSpecialModuleToggle(Module module) {
        // Refresh UI for modules that affect other buttons
        if (module instanceof ShowKeystrokesModule || module instanceof ProjectileTrailModule) {
            refreshUI();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client.world != null) {
            BlurHandler.executeBlur(context, this.width, this.height, 2.0f);
            super.render(context, mouseX, mouseY, deltaTicks);
        } else {
            renderBackgroundAnimation(context, mouseX, mouseY);
            super.render(context, mouseX, mouseY, deltaTicks);
        }

        renderTitleAndCategoryHeaders(context);
        renderTrailPreview(context);
        renderTooltips(context, mouseX, mouseY);
    }

    /**
     * Render animated dot background (when not in-game)
     */
    private void renderBackgroundAnimation(DrawContext context, int mouseX, int mouseY) {
        context.fill(0, 0, this.width, this.height, 0xFF101010);

        // Update and render dots
        for (Dot dot : dots) {
            dot.update(this.width, this.height, mouseX, mouseY);
            context.fill((int) dot.x, (int) dot.y, (int) dot.x + 2, (int) dot.y + 2, 0x55FFFFFF);
        }

        // Render connections between nearby dots
        for (int i = 0; i < dots.size(); i++) {
            Dot a = dots.get(i);
            for (int j = i + 1; j < dots.size(); j++) {
                Dot b = dots.get(j);
                double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));

                if (dist < 25) {
                    int alpha = (int) ((1.0 - (dist / 50.0)) * 100);
                    int color = (alpha << 24) | 0xFFFFFF;
                    context.fill((int) a.x, (int) a.y, (int) b.x, (int) b.y + 1, color);
                }
            }
        }
    }

    /**
     * Render title and category headers with icons
     */
    private void renderTitleAndCategoryHeaders(DrawContext context) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) - 62;

        // Main title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFFFF);

        // Category headers
        for (Category category : Category.values()) {
            int columnX = category.getColumnX(centerX, BUTTON_WIDTH);
            int headerY = centerY - 40;

            // Category text
            if (category == Category.RENDER) {
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal(category.getDisplayName()).setStyle(font),
                        centerX - 10,
                        headerY,
                        category.getColor()
                );
                context.drawItem(new ItemStack(category.getIconItem()), centerX - 6, centerY - 62);
            } else {
                context.drawTextWithShadow(
                        this.textRenderer,
                        Text.literal(category.getDisplayName()).setStyle(font),
                        columnX + 64,
                        headerY,
                        category.getColor()
                );
                context.drawItem(new ItemStack(category.getIconItem()), columnX + 39, centerY - 45);
            }
        }
    }

    /**
     * Render trail preview icon
     */
    private void renderTrailPreview(DrawContext context) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) - 62;
        int previewX = centerX + 83;
        int previewY = centerY + 78;

        ItemStack previewStack = switch (TrailSettings.getTrailIndex()) {
            case 0 -> new ItemStack(Items.TOTEM_OF_UNDYING);
            case 1 -> new ItemStack(Items.TNT);
            case 3 -> new ItemStack(Items.STICK);
            default -> ItemStack.EMPTY;
        };

        if (!previewStack.isEmpty()) {
            context.drawItem(previewStack, previewX, previewY);
        } else if (TrailSettings.getTrailIndex() == 2) {
            context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED,
                    Identifier.ofVanilla("hud/heart/full"),
                    previewX,
                    previewY + 2,
                    11,
                    11,
                    0xFFFFFFFF
            );
        }
    }

    /**
     * Render custom tooltips for buttons
     */
    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        for (Element element : this.children()) {
            if (element instanceof ModernButton button && button.shouldRenderTooltip(mouseX, mouseY)) {
                button.renderCustomTooltip(context, mouseX, mouseY, button.getTooltipAlpha());
            }
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

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        if (click.isLeft()) {
            for (Dot dot : dots) {
                dot.applyShake();
            }
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    /**
     * Legacy compatibility - get module state for other systems
     */
    public static boolean isModuleEnabled(String moduleName) {
        Module module = ModuleManager.getInstance().getModule(moduleName);
        return module != null && module.isEnabled();
    }
}