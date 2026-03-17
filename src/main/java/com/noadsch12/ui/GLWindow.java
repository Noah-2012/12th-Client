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

package com.noadsch12.ui;

import com.noadsch12.util.BasicGlobals;
import com.noadsch12.event.EventBus;
import com.noadsch12.event.events.MouseScrollEvent;
import com.noadsch12.event.events.TickEvent;
import com.noadsch12.event.listeners.MouseScrollListener;
import com.noadsch12.event.listeners.TickListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GLWindow — Modern flat-style draggable overlay window with full widget library.
 * Target: Fabric 1.21.10, Yarn mappings.
 *
 * Drop into any Screen via Mixin — see TitleScreenMixin for usage.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  Widgets available:                                             │
 * │   addLabel(text, x, y)                                         │
 * │   addSeparator(y)                                              │
 * │   addButton(label, x, y, w, h, onClick)                        │
 * │   addCheckbox(label, x, y, checked, onChange)                  │
 * │   addTextField(x, y, w, placeholder, onChange)                 │
 * │   addSlider(label, x, y, w, min, max, value, onChange)         │
 * │   addDropdown(label, x, y, w, options, selectedIndex, onChange) │
 * │   addScrollPane(x, y, w, h)  → returns GLScrollPane            │
 * └─────────────────────────────────────────────────────────────────┘
 */
public class GLWindow implements TickListener, MouseScrollListener {

    // ── Font ──────────────────────────────────────────────────────────────────
    /** Arial font style applied to every text draw call in this UI. */
    public static final Style FONT = Style.EMPTY.withFont(new StyleSpriteSource.Font(BasicGlobals.ARIAL_FONT));

    // ── Dimensions ────────────────────────────────────────────────────────────
    private int windowW = 200;
    private int windowH = 500;
    public static final int TITLE_H  = 13;

    private static final int BTN_W            = 12;
    private static final int BTN_H            = 10;
    private static final int BTN_MARGIN_RIGHT = 3;
    private static final int BTN_MARGIN_TOP   = 2;

    // ── Colour palette (ARGB 0xAARRGGBB) ─────────────────────────────────────
    // Window chrome
    private static final int COL_BORDER        = 0xFF2A2A2A;
    private static final int COL_TITLE_BG      = 0xFF1E1E2E;
    private static final int COL_TITLE_LINE    = 0xFF3D5AFE;
    private static final int COL_TITLE_TXT     = 0xFFE0E0FF;
    private static final int COL_BODY          = 0xFF252535;
    private static final int COL_BODY_LINE     = 0xFF1A1A28;
    // Close button
    private static final int COL_BTN_NORMAL    = 0xFF2A2A3E;
    private static final int COL_BTN_HOVER     = 0xFFE53935;
    private static final int COL_X_NORMAL      = 0xFF8888AA;
    private static final int COL_X_HOVER       = 0xFFFFFFFF;
    // Widgets — shared accent
    static final int COL_ACCENT        = 0xFF3D5AFE;  // blue accent
    static final int COL_ACCENT_HOVER  = 0xFF6680FF;
    static final int COL_WIDGET_BG     = 0xFF1A1A28;
    static final int COL_WIDGET_BORDER = 0xFF3A3A5A;
    static final int COL_WIDGET_TEXT   = 0xFFCCCCDD;
    static final int COL_WIDGET_HINT   = 0xFF555570;
    static final int COL_SEPARATOR     = 0xFF2E2E48;
    static final int COL_SLIDER_TRACK  = 0xFF1A1A28;
    static final int COL_SLIDER_FILL   = 0xFF3D5AFE;
    static final int COL_SLIDER_THUMB  = 0xFFE0E0FF;
    static final int COL_CHECK_BG      = 0xFF1A1A28;
    static final int COL_CHECK_TICK    = 0xFF3D5AFE;
    static final int COL_DD_BG         = 0xFF1A1A28;
    static final int COL_DD_HOVER_ROW  = 0xFF2E2E4A;

    // ── Window state ──────────────────────────────────────────────────────────
    private final String title;
    private       int     wx, wy;
    private       boolean visible  = true;
    private       boolean dragging = false;
    private       int     dragOx, dragOy;

    // ── Widget list ───────────────────────────────────────────────────────────
    private final List<GLWidget> widgets = new ArrayList<>();

    // ── Currently focused text field ──────────────────────────────────────────
    private GLTextField focusedField = null;

    // ── Constructor ───────────────────────────────────────────────────────────
    public GLWindow(String title, int x, int y) {
        this.title = title;
        this.wx = x;
        this.wy = y;
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public boolean isVisible()               { return visible;  }
    public void setVisible(boolean visible) {
        if (this.visible == visible) return; // no change, do nothing

        this.visible = visible;

        if (visible) {
            EventBus.register(this);
        } else {
            EventBus.unregister(this);
        }
    }
    public void    show()                    { visible = true;  }
    public void    hide()                    { visible = false; }
    public void    setPosition(int x, int y) { wx = x; wy = y;  }
    public void    setDimensions(int width, int height) { windowW = width; windowH = height; }

    /** Remove all widgets. */
    public void clearWidgets() { widgets.clear(); }

    // ── Widget factory methods ────────────────────────────────────────────────

    /**
     * Plain text label.
     *
     * @param text label string
     * @param x    x offset relative to window body (inside left border)
     * @param y    y offset relative to window body top
     */
    public GLLabel addLabel(String text, int x, int y) {
        GLLabel w = new GLLabel(this, text, x, y);
        widgets.add(w);
        return w;
    }

    /**
     * Horizontal separator line.
     *
     * @param y y offset relative to window body top
     */
    public GLSeparator addSeparator(int y) {
        GLSeparator w = new GLSeparator(this, y);
        widgets.add(w);
        return w;
    }

    /**
     * Clickable flat button.
     *
     * @param label   button text
     * @param x, y    position relative to window body
     * @param w, h    size in pixels
     * @param onClick called when clicked
     */
    public GLButton addButton(String label, int x, int y, int w, int h, Runnable onClick) {
        GLButton btn = new GLButton(this, label, x, y, w, h, onClick);
        widgets.add(btn);
        return btn;
    }

    /**
     * Toggle checkbox.
     *
     * @param label    text shown next to box
     * @param x, y     position relative to window body
     * @param checked  initial state
     * @param onChange called with new boolean value
     */
    public GLCheckbox addCheckbox(String label, int x, int y, boolean checked,
                                  Consumer<Boolean> onChange) {
        GLCheckbox cb = new GLCheckbox(this, label, x, y, checked, onChange);
        widgets.add(cb);
        return cb;
    }

    /**
     * Single-line text input field.
     *
     * @param x, y        position relative to window body
     * @param w           width in pixels
     * @param placeholder hint text shown when empty
     * @param onChange    called with current string on every keystroke
     */
    public GLTextField addTextField(int x, int y, int w, String placeholder,
                                    Consumer<String> onChange) {
        GLTextField tf = new GLTextField(this, x, y, w, placeholder, onChange);
        widgets.add(tf);
        return tf;
    }

    /**
     * Horizontal drag slider.
     *
     * @param label    label drawn above the track
     * @param x, y     position relative to window body
     * @param w        track width in pixels
     * @param min, max value range (integers)
     * @param value    initial value
     * @param onChange called with new integer value on drag
     */
    public GLSlider addSlider(String label, int x, int y, int w,
                              double min, double max, double value,
                              Consumer<Double> onChange) {        // ← Double
        GLSlider sl = new GLSlider(this, label, x, y, w, min, max, value, onChange);
        widgets.add(sl);
        return sl;
    }

    /**
     * Drop-down / combo-box.
     *
     * @param label         label drawn left of the box
     * @param x, y          position relative to window body
     * @param w             width of the combo box
     * @param options       list of choice strings
     * @param selectedIndex initial selection (0-based)
     * @param onChange      called with new selected index
     */
    public GLDropdown addDropdown(String label, int x, int y, int w,
                                  List<String> options, int selectedIndex,
                                  Consumer<Integer> onChange) {
        GLDropdown dd = new GLDropdown(this, label, x, y, w, options, selectedIndex, onChange);
        widgets.add(dd);
        return dd;
    }

    /**
     * Scroll pane — add child widgets to the returned object.
     *
     * @param x, y    position relative to window body
     * @param w, h    visible size of the pane
     */
    public GLScrollPane addScrollPane(int x, int y, int w, int h) {
        GLScrollPane sp = new GLScrollPane(this, x, y, w, h);
        widgets.add(sp);
        return sp;
    }

    // ── Widget coordinate helpers (package-private) ────────────────────────────
    /** Absolute x of the window body's inner left edge. */
    int bodyX() { return wx + 2; }
    /** Absolute y of the window body's top edge. */
    int bodyY() { return wy + TITLE_H + 2; }

    // ── Input ─────────────────────────────────────────────────────────────────

    /**
     * Call from your Mixin's mouseClicked override.
     */
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!visible) return false;
        int ix = (int) click.x();
        int iy = (int) click.y();
        int btn = click.button();
        if (btn != 0) return false;

        for (int i = widgets.size() - 1; i >= 0; i--) {
            GLWidget w = widgets.get(i);
            if (w instanceof GLDropdown dd && dd.isOpen()) {
                if (dd.mouseClicked(ix, iy)) return true;
            }
        }

        if (isOverClose(ix, iy)) { visible = false; return true; }
        if (isOverTitle(ix, iy)) {
            dragging = true;
            dragOx = ix - wx;
            dragOy = iy - wy;
            // Clicking title defocuses any text field
            if (focusedField != null) { focusedField.setFocused(false); focusedField = null; }
            return true;
        }

        if (!isOverWindow(ix, iy)) return false;

        // Defocus text field if user clicks elsewhere
        if (focusedField != null) { focusedField.setFocused(false); focusedField = null; }

        // Pass to widgets in reverse paint order so topmost (dropdown) gets first pick
        for (int i = widgets.size() - 1; i >= 0; i--) {
            GLWidget w = widgets.get(i);

            if (w instanceof GLDropdown dd && dd.isOpen()) continue;

            if (w.mouseClicked(ix, iy)) {
                if (w instanceof GLTextField tf) { focusedField = tf; }
                return true;
            }
        }
        return true; // consumed — window owns the area
    }

    public boolean onMouseButton(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        int ix = (int) mouseX;
        int iy = (int) mouseY;

        if (button == 0) {
            // Close button
            if (isOverClose(ix, iy)) {
                setVisible(false);
                return true;
            }

            // Title bar — start dragging
            if (isOverTitle(ix, iy)) {
                dragging = true;
                dragOx = ix - wx;
                dragOy = iy - wy;
                if (focusedField != null) {
                    focusedField.setFocused(false);
                    focusedField = null;
                }
                return true;
            }
        }


        // Not over window at all — don't consume
        if (!isOverWindow(ix, iy)) return false;

        // Defocus text field on click elsewhere
        if (focusedField != null) {
            focusedField.setFocused(false);
            focusedField = null;
        }

        // Forward to widgets — dropdowns first (reverse order, open ones prioritised)
        for (int i = widgets.size() - 1; i >= 0; i--) {
            GLWidget w = widgets.get(i);
            if (w instanceof GLDropdown dd && dd.isOpen()) {
                if (dd.mouseClicked(ix, iy)) return true;
            }
        }

        for (int i = widgets.size() - 1; i >= 0; i--) {
            GLWidget w = widgets.get(i);
            if (w instanceof GLDropdown dd && dd.isOpen()) continue;
            if (w.mouseClicked(ix, iy)) {
                if (w instanceof GLTextField tf) focusedField = tf;
                return true;
            }
        }

        return true; // consume — window owns this area
    }

    public void onMouseRelease() {
        dragging = false;
        for (GLWidget w : widgets) {
            if (w instanceof GLSlider sl) sl.release();
        }
    }

    /**
     * Call from your Mixin's mouseScrolled override.
     */
    @Override
    public void onMouseScroll(MouseScrollEvent event) {
        if (!visible) return;

        double mx = event.GetMouseX();
        double my = event.GetMouseY();
        double dx = event.GetHorizontal();
        double dy = event.GetVertical();

        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseScrolled(mx, my, dx, dy)) {
                event.cancel();
                return;
            }
        }
    }

    /**
     * Call from your Mixin's mouseDragged override.
     */
    public void onMouseDragged(double mouseX, double mouseY) {
        if (!visible) return;

        // Update window dragging
        if (dragging) {
            wx = (int) mouseX - dragOx;
            wy = (int) mouseY - dragOy;
        }

        // Forward to widgets (sliders need this)
        int ix = (int) mouseX;
        int iy = (int) mouseY;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            // button=0, dx/dy=0 since we pass absolute position
            if (widgets.get(i).mouseDragged(ix, iy, 0, 0, 0)) break;
        }
    }

    /**
     * Call from your Mixin's keyPressed override.
     * Returns true if the key was consumed.
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible || focusedField == null) return false;
        return focusedField.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Call from your Mixin's charTyped override.
     * Returns true if the character was consumed.
     */
    public boolean charTyped(char chr, int modifiers) {
        if (!visible || focusedField == null) return false;
        return focusedField.charTyped(chr, modifiers);
    }

    // ── Render ────────────────────────────────────────────────────────────────
    public void render(DrawContext ctx, int mouseX, int mouseY) {
        if (!visible) return;

        // NOTE: This gets handled in the mouseDragged Method
        //if (dragging) { wx = mouseX - dragOx; wy = mouseY - dragOy; }

        boolean hov = isOverClose(mouseX, mouseY);

        // 1. Outer border
        fill(ctx, wx,                wy,                windowW, 1,        COL_BORDER);
        fill(ctx, wx,                wy + windowH - 1, windowW, 1,        COL_BORDER);
        fill(ctx, wx,                wy,                1,        windowH, COL_BORDER);
        fill(ctx, wx + windowW - 1, wy,                1,        windowH, COL_BORDER);

        // 2. Title bar
        fill(ctx, wx + 1, wy + 1, windowW - 2, TITLE_H - 1, COL_TITLE_BG);

        // 3. Blue accent line
        fill(ctx, wx + 1, wy + TITLE_H, windowW - 2, 1, COL_TITLE_LINE);

        // 4. Body
        fill(ctx, wx + 1, wy + TITLE_H + 1, windowW - 2, windowH - TITLE_H - 2, COL_BODY);

        // 5. Inner body border
        fill(ctx, wx + 1,            wy + TITLE_H + 1, 1,             windowH - TITLE_H - 2, COL_BODY_LINE);
        fill(ctx, wx + windowW - 2, wy + TITLE_H + 1, 1,             windowH - TITLE_H - 2, COL_BODY_LINE);
        fill(ctx, wx + 1,            wy + windowH - 2, windowW - 2, 1,                       COL_BODY_LINE);

        // 6. Close button
        drawCloseBtn(ctx, hov);

        // 7. Title text (Arial font)
        MinecraftClient mc  = MinecraftClient.getInstance();
        int maxW = windowW - BTN_W - BTN_MARGIN_RIGHT - 10;
        String txt = mc.textRenderer.trimToWidth(title, maxW);
        int ty = wy + (TITLE_H - mc.textRenderer.fontHeight) / 2;
        ctx.drawText(mc.textRenderer, Text.literal(txt).setStyle(FONT), wx + 5, ty, COL_TITLE_TXT, false);

        // 8. Render widgets (non-dropdown pass first, then dropdown on top)
        for (GLWidget w : widgets) {
            if (!(w instanceof GLDropdown)) w.render(ctx, mouseX, mouseY);
        }
        for (GLWidget w : widgets) {
            if (w instanceof GLDropdown) w.render(ctx, mouseX, mouseY);
        }
    }

    // ── Close button ──────────────────────────────────────────────────────────
    private void drawCloseBtn(DrawContext ctx, boolean hov) {
        int bx = wx + windowW - BTN_W - BTN_MARGIN_RIGHT - 1;
        int by = wy + BTN_MARGIN_TOP;
        fill(ctx, bx, by, BTN_W, BTN_H, hov ? COL_BTN_HOVER : COL_BTN_NORMAL);
        int xc = hov ? COL_X_HOVER : COL_X_NORMAL;
        int px = bx + 3, py = by + 2, sz = 5;
        for (int i = 0; i < sz; i++) {
            fill(ctx, px + i,       py + i, 1, 1, xc);
            fill(ctx, px + sz-1-i,  py + i, 1, 1, xc);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    static void fill(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    private boolean isOverWindow(int mx, int my) {
        return mx >= wx && mx < wx + windowW && my >= wy && my < wy + windowH;
    }
    private boolean isOverTitle(int mx, int my) {
        return mx >= wx && mx < wx + windowW && my >= wy && my < wy + TITLE_H;
    }
    private boolean isOverClose(int mx, int my) {
        int bx = wx + windowW - BTN_W - BTN_MARGIN_RIGHT - 1;
        int by = wy + BTN_MARGIN_TOP;
        return mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H;
    }

    // ── TickListener ──────────────────────────────────────────────────────────
    @Override
    public void onTick(TickEvent.Pre event) {}

    @Override
    public void onTick(TickEvent.Post event) {

        // Blink cursor for focused text field
        for (GLWidget w : widgets) if (w instanceof GLTextField tf) tf.tick();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Base widget
    // ════════════════════════════════════════════════════════════════════════════

    /** Abstract base for all widgets. Coordinates are relative to body origin. */
    public abstract static class GLWidget {
        protected final GLWindow win;
        /** x relative to window body inner-left */
        protected int rx, ry;

        protected GLWidget(GLWindow win, int rx, int ry) {
            this.win = win;
            this.rx = rx;
            this.ry = ry;
        }

        /** Absolute x on screen. */
        protected int ax() { return win.bodyX() + rx; }
        /** Absolute y on screen. */
        protected int ay() { return win.bodyY() + ry; }

        /** Called with absolute mouse coords. Returns true to consume. */
        public boolean mouseClicked(int mx, int my)                          { return false; }
        public boolean mouseScrolled(double mx, double my, double dx, double dy) { return false; }
        public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { return false; }

        public abstract void render(DrawContext ctx, int mx, int my);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLLabel
    // ════════════════════════════════════════════════════════════════════════════

    /** Simple non-interactive text label. */
    public static class GLLabel extends GLWidget {
        private String text;
        private int    color;

        public GLLabel(GLWindow win, String text, int rx, int ry) {
            super(win, rx, ry);
            this.text  = text;
            this.color = COL_WIDGET_TEXT;
        }

        public GLLabel setText(String t) { this.text = t; return this; }
        public GLLabel setColor(int c)   { this.color = c; return this; }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ctx.drawText(mc.textRenderer, Text.literal(text).setStyle(FONT), ax(), ay(), color, false);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLSeparator
    // ════════════════════════════════════════════════════════════════════════════

    /** 1-pixel horizontal rule spanning the window body width. */
    public static class GLSeparator extends GLWidget {
        public GLSeparator(GLWindow win, int ry) { super(win, 0, ry); }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            int bodyW = win.windowW - 4; // body width between inner borders
            fill(ctx, ax(), ay(), bodyW, 1, COL_SEPARATOR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLButton
    // ════════════════════════════════════════════════════════════════════════════

    /** Flat, accent-coloured clickable button. */
    public static class GLButton extends GLWidget {
        private String   label;
        private final int bw, bh;
        private Runnable onClick;
        private boolean  hovered;

        public GLButton(GLWindow win, String label, int rx, int ry, int bw, int bh,
                        Runnable onClick) {
            super(win, rx, ry);
            this.label   = label;
            this.bw      = bw;
            this.bh      = bh;
            this.onClick = onClick;
        }

        public GLButton setLabel(String l)       { label   = l;  return this; }
        public GLButton setOnClick(Runnable r)   { onClick = r;  return this; }

        @Override
        public boolean mouseClicked(int mx, int my) {
            if (isOver(mx, my)) { if (onClick != null) onClick.run(); return true; }
            return false;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            hovered = isOver(mx, my);
            int bg = hovered ? COL_ACCENT_HOVER : COL_ACCENT;
            fill(ctx, ax(), ay(), bw, bh, bg);
            MinecraftClient mc = MinecraftClient.getInstance();
            String trimmed = mc.textRenderer.trimToWidth(label, bw - 4);
            int tx = ax() + (bw - mc.textRenderer.getWidth(trimmed)) / 2;
            int ty = ay() + (bh - mc.textRenderer.fontHeight) / 2;
            ctx.drawText(mc.textRenderer, Text.literal(trimmed).setStyle(FONT), tx, ty, 0xFFFFFFFF, false);
        }

        private boolean isOver(int mx, int my) {
            return mx >= ax() && mx < ax() + bw && my >= ay() && my < ay() + bh;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLCheckbox
    // ════════════════════════════════════════════════════════════════════════════

    /** Toggle checkbox with label. Box is 9×9 pixels. */
    public static class GLCheckbox extends GLWidget {
        private static final int BOX = 9;

        private final String           label;
        private       boolean          checked;
        private final Consumer<Boolean> onChange;

        public GLCheckbox(GLWindow win, String label, int rx, int ry,
                          boolean checked, Consumer<Boolean> onChange) {
            super(win, rx, ry);
            this.label    = label;
            this.checked  = checked;
            this.onChange = onChange;
        }

        public boolean isChecked()         { return checked; }
        public void    setChecked(boolean b) {
            checked = b;
            if (onChange != null) onChange.accept(b);
        }

        @Override
        public boolean mouseClicked(int mx, int my) {
            if (isOver(mx, my)) { setChecked(!checked); return true; }
            return false;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            boolean hov = isOver(mx, my);
            int border = hov ? COL_ACCENT : COL_WIDGET_BORDER;

            // Box border
            fill(ctx, ax(),       ay(),       BOX, 1,   border);
            fill(ctx, ax(),       ay()+BOX-1, BOX, 1,   border);
            fill(ctx, ax(),       ay(),       1,   BOX, border);
            fill(ctx, ax()+BOX-1, ay(),       1,   BOX, border);
            // Box fill
            fill(ctx, ax()+1, ay()+1, BOX-2, BOX-2, COL_CHECK_BG);

            // Check mark — simple "√" via pixel lines
            if (checked) {
                fill(ctx, ax()+2, ay()+5, 1, 1, COL_CHECK_TICK);
                fill(ctx, ax()+3, ay()+6, 1, 1, COL_CHECK_TICK);
                fill(ctx, ax()+4, ay()+5, 1, 1, COL_CHECK_TICK);
                fill(ctx, ax()+5, ay()+4, 1, 1, COL_CHECK_TICK);
                fill(ctx, ax()+6, ay()+3, 1, 1, COL_CHECK_TICK);
                fill(ctx, ax()+7, ay()+2, 1, 1, COL_CHECK_TICK);
            }

            // Label (Arial font)
            MinecraftClient mc = MinecraftClient.getInstance();
            int ty = ay() + (BOX - mc.textRenderer.fontHeight) / 2;
            ctx.drawText(mc.textRenderer, Text.literal(label).setStyle(FONT),
                    ax() + BOX + 4, ty, COL_WIDGET_TEXT, false);
        }

        private boolean isOver(int mx, int my) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int labelW = mc.textRenderer.getWidth(label);
            int totalW = BOX + 4 + labelW;
            return mx >= ax() && mx < ax() + totalW && my >= ay() && my < ay() + BOX;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLTextField
    // ════════════════════════════════════════════════════════════════════════════

    /** Single-line text input with cursor and placeholder support. */
    public static class GLTextField extends GLWidget {
        private static final int HEIGHT = 12;
        private static final int PAD    = 3;

        private final int            fw;
        private final String         placeholder;
        private final Consumer<String> onChange;

        private StringBuilder text     = new StringBuilder();
        private int           cursor   = 0;
        private boolean       focused  = false;
        private int           blinkTimer = 0;
        private boolean       cursorVisible = true;

        // Horizontal scroll offset in pixels
        private int scrollOffset = 0;

        public GLTextField(GLWindow win, int rx, int ry, int fw,
                           String placeholder, Consumer<String> onChange) {
            super(win, rx, ry);
            this.fw          = fw;
            this.placeholder = placeholder;
            this.onChange    = onChange;
        }

        public String  getValue()        { return text.toString(); }
        public void    setValue(String s){ text = new StringBuilder(s); cursor = s.length(); notifyChange(); }
        public boolean isFocused()       { return focused; }
        public void    setFocused(boolean f) { focused = f; }

        /** Called every tick to blink the cursor. */
        public void tick() {
            if (!focused) return;
            blinkTimer++;
            if (blinkTimer >= 10) { blinkTimer = 0; cursorVisible = !cursorVisible; }
        }

        @Override
        public boolean mouseClicked(int mx, int my) {
            if (mx >= ax() && mx < ax() + fw && my >= ay() && my < ay() + HEIGHT) {
                focused = true;
                cursorVisible = true;
                blinkTimer = 0;
                // Position cursor at click
                MinecraftClient mc = MinecraftClient.getInstance();
                int clickX = mx - ax() - PAD + scrollOffset;
                cursor = 0;
                for (int i = 1; i <= text.length(); i++) {
                    if (mc.textRenderer.getWidth(text.substring(0, i)) <= clickX) cursor = i;
                }
                return true;
            }
            focused = false;
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!focused) return false;
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cursor > 0) { text.deleteCharAt(cursor - 1); cursor--; notifyChange(); }
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (cursor < text.length()) { text.deleteCharAt(cursor); notifyChange(); }
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT  -> { if (cursor > 0) cursor--; return true; }
                case GLFW.GLFW_KEY_RIGHT -> { if (cursor < text.length()) cursor++; return true; }
                case GLFW.GLFW_KEY_HOME  -> { cursor = 0; return true; }
                case GLFW.GLFW_KEY_END   -> { cursor = text.length(); return true; }
                default -> { return false; }
            }
        }

        public boolean charTyped(char chr, int modifiers) {
            if (!focused) return false;
            if (chr >= 32 && chr != 127) {
                text.insert(cursor, chr);
                cursor++;
                notifyChange();
                return true;
            }
            return false;
        }

        private void notifyChange() { if (onChange != null) onChange.accept(text.toString()); }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int borderColor = focused ? COL_ACCENT : COL_WIDGET_BORDER;

            // Border
            fill(ctx, ax(),         ay(),           fw, 1,      borderColor);
            fill(ctx, ax(),         ay()+HEIGHT-1,  fw, 1,      borderColor);
            fill(ctx, ax(),         ay(),           1,  HEIGHT, borderColor);
            fill(ctx, ax()+fw-1,    ay(),           1,  HEIGHT, borderColor);
            // Fill
            fill(ctx, ax()+1, ay()+1, fw-2, HEIGHT-2, COL_WIDGET_BG);

            // Enable scissor to clip text inside field
            ctx.enableScissor(ax()+PAD, ay()+1, ax()+fw-PAD, ay()+HEIGHT-1);

            int ty = ay() + (HEIGHT - mc.textRenderer.fontHeight) / 2;

            if (text.isEmpty() && !focused) {
                // Placeholder (Arial font)
                ctx.drawText(mc.textRenderer, Text.literal(placeholder).setStyle(FONT),
                        ax() + PAD - scrollOffset, ty, COL_WIDGET_HINT, false);
            } else {
                // Keep cursor visible by scrolling
                String beforeCursor = text.substring(0, cursor);
                int cursorPx = mc.textRenderer.getWidth(beforeCursor);
                int innerW = fw - PAD * 2;
                if (cursorPx - scrollOffset > innerW) scrollOffset = cursorPx - innerW;
                if (cursorPx - scrollOffset < 0)      scrollOffset = cursorPx;

                // Input text (Arial font)
                ctx.drawText(mc.textRenderer, Text.literal(text.toString()).setStyle(FONT),
                        ax() + PAD - scrollOffset, ty, COL_WIDGET_TEXT, false);

                // Cursor line
                if (focused && cursorVisible) {
                    int cx = ax() + PAD + cursorPx - scrollOffset;
                    fill(ctx, cx, ay() + 2, 1, HEIGHT - 4, COL_ACCENT);
                }
            }

            ctx.disableScissor();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLSlider
    // ════════════════════════════════════════════════════════════════════════════

    /** Horizontal integer-range slider with a draggable thumb. */
    public static class GLSlider extends GLWidget {
        private static final int TRACK_H = 3;
        private static final int THUMB_W = 6;
        private static final int THUMB_H = 10;
        private static final int LABEL_H = 9;

        private final String label;
        private final int sw;
        private final double min, max;
        private double value;
        private final Consumer<Double> onChange;  // ← Double instead of Integer

        private boolean dragging = false;

        public GLSlider(GLWindow win, String label, int rx, int ry, int sw,
                        double min, double max, double value, Consumer<Double> onChange) {
            super(win, rx, ry);
            this.label    = label;
            this.sw       = sw;
            this.min      = min;
            this.max      = max;
            this.value    = Math.max(min, Math.min(max, value));
            this.onChange = onChange;
        }

        public double getValue()        { return value; }
        public void setValue(double v)  { value = clamp(v); }
        public void release()           { dragging = false; }

        @Override
        public boolean mouseClicked(int mx, int my) {
            if (isOverTrackArea(mx, my)) {
                dragging = true;
                setFromX(mx);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
            if (!dragging) return false;
            setFromX((int) mx);
            return true;
        }

        private void setFromX(int mx) {
            int trackX = ax();
            int range  = sw - THUMB_W;
            int relX   = mx - trackX - THUMB_W / 2;
            double t   = (double) relX / range;
            t          = Math.max(0.0, Math.min(1.0, t)); // clamp t to [0,1]
            value      = clamp(min + t * (max - min));
            if (onChange != null) onChange.accept(value);
        }

        private double clamp(double v) { return Math.max(min, Math.min(max, v)); }

        private boolean isOverTrackArea(int mx, int my) {
            return mx >= ax() && mx < ax() + sw
                    && my >= ay() + LABEL_H && my < ay() + LABEL_H + THUMB_H;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int trackY = ay() + LABEL_H + (THUMB_H - TRACK_H) / 2;

            // Label + current value rounded to 2 decimal places
            String display = label + ": " + Math.round(value * 100.0) / 100.0;
            ctx.drawText(mc.textRenderer, Text.literal(display).setStyle(FONT), ax(), ay(), COL_WIDGET_TEXT, false);

            // Track background
            fill(ctx, ax(), trackY, sw, TRACK_H, COL_SLIDER_TRACK);

            // Filled portion
            double t   = (max == min) ? 0 : (value - min) / (max - min);
            int fillW  = (int)(t * (sw - THUMB_W)) + THUMB_W / 2;
            fill(ctx, ax(), trackY, fillW, TRACK_H, COL_SLIDER_FILL);

            // Thumb
            int thumbX = ax() + (int)(t * (sw - THUMB_W));
            int thumbY = ay() + LABEL_H;
            fill(ctx, thumbX,   thumbY,   THUMB_W,     THUMB_H,     COL_WIDGET_BORDER);
            fill(ctx, thumbX+1, thumbY+1, THUMB_W - 2, THUMB_H - 2, COL_SLIDER_THUMB);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLDropdown
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Combo-box / dropdown.  When open, the option list is drawn on top of all
     * other widgets (the window's render loop draws dropdowns last).
     */
    public static class GLDropdown extends GLWidget {
        private static final int HEIGHT     = 12;
        private static final int ROW_H      = 11;
        private static final int ARROW_SIZE = 5;

        private final String           label;
        private final int              dw;
        private final List<String>     options;
        private       int              selected;
        private final Consumer<Integer> onChange;
        private       boolean          open = false;

        public GLDropdown(GLWindow win, String label, int rx, int ry, int dw,
                          List<String> options, int selected, Consumer<Integer> onChange) {
            super(win, rx, ry);
            this.label    = label;
            this.dw       = dw;
            this.options  = options;
            this.selected = Math.max(0, Math.min(options.size() - 1, selected));
            this.onChange = onChange;
        }

        public int    getSelected()      { return selected; }
        public String getSelectedOption(){ return options.isEmpty() ? "" : options.get(selected); }
        public boolean isOpen()          { return open; }

        @Override
        public boolean mouseClicked(int mx, int my) {
            // Click on the combo box header
            if (isOverHeader(mx, my)) {
                open = !open;
                return true;
            }
            // Click in the open dropdown list
            if (open) {
                for (int i = 0; i < options.size(); i++) {
                    if (isOverRow(mx, my, i)) {
                        selected = i;
                        open = false;
                        if (onChange != null) onChange.accept(selected);
                        return true;
                    }
                }
                // Click outside — close
                open = false;
                return true;
            }
            return false;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            MinecraftClient mc = MinecraftClient.getInstance();

            // Label to the left (Arial font)
            if (!label.isEmpty()) {
                int ty = ay() + (HEIGHT - mc.textRenderer.fontHeight) / 2;
                ctx.drawText(mc.textRenderer, Text.literal(label).setStyle(FONT),
                        ax(), ty, COL_WIDGET_TEXT, false);
            }

            int bx = boxX();

            // Header box border
            int borderColor = open ? COL_ACCENT : COL_WIDGET_BORDER;
            fill(ctx, bx,         ay(),          dw, 1,      borderColor);
            fill(ctx, bx,         ay()+HEIGHT-1, dw, 1,      borderColor);
            fill(ctx, bx,         ay(),          1,  HEIGHT, borderColor);
            fill(ctx, bx+dw-1,    ay(),          1,  HEIGHT, borderColor);
            // Header fill
            fill(ctx, bx+1, ay()+1, dw-2, HEIGHT-2, COL_DD_BG);

            // Selected text (Arial font)
            String sel = options.isEmpty() ? "" : mc.textRenderer.trimToWidth(options.get(selected), dw - ARROW_SIZE - 8);
            int ty = ay() + (HEIGHT - mc.textRenderer.fontHeight) / 2;
            ctx.drawText(mc.textRenderer, Text.literal(sel).setStyle(FONT), bx + 3, ty, COL_WIDGET_TEXT, false);

            // Arrow "▾"
            int ax = bx + dw - ARROW_SIZE - 3;
            int ay = ay() + (HEIGHT - 1) / 2;
            if (open) {
                // Up arrow ▴ — two rows of pixels
                fill(ctx, ax+2, ay-1, 1, 1, COL_WIDGET_TEXT);
                fill(ctx, ax+1, ay,   3, 1, COL_WIDGET_TEXT);
                fill(ctx, ax,   ay+1, 5, 1, COL_WIDGET_TEXT);
            } else {
                // Down arrow ▾
                fill(ctx, ax,   ay-1, 5, 1, COL_WIDGET_TEXT);
                fill(ctx, ax+1, ay,   3, 1, COL_WIDGET_TEXT);
                fill(ctx, ax+2, ay+1, 1, 1, COL_WIDGET_TEXT);
            }

            // Open list
            if (open) {
                int listY = ay() + HEIGHT;
                int listH = options.size() * ROW_H;

                // List background with border
                fill(ctx, bx,   listY,       dw, listH + 2, COL_ACCENT);       // border
                fill(ctx, bx+1, listY+1,     dw-2, listH,   COL_DD_BG);       // fill

                for (int i = 0; i < options.size(); i++) {
                    int ry2 = listY + 1 + i * ROW_H;
                    boolean rowHov = isOverRow(mx, my, i);
                    boolean rowSel = (i == selected);

                    if (rowSel || rowHov)
                        fill(ctx, bx+1, ry2, dw-2, ROW_H, rowSel ? COL_ACCENT : COL_DD_HOVER_ROW);

                    // Option text (Arial font)
                    String opt = mc.textRenderer.trimToWidth(options.get(i), dw - 6);
                    int tty = ry2 + (ROW_H - mc.textRenderer.fontHeight) / 2;
                    ctx.drawText(mc.textRenderer, Text.literal(opt).setStyle(FONT),
                            bx + 3, tty,
                            rowSel ? 0xFFFFFFFF : COL_WIDGET_TEXT, false);
                }
            }
        }

        private int boxX() {
            MinecraftClient mc = MinecraftClient.getInstance();
            return label.isEmpty() ? ax() : ax() + mc.textRenderer.getWidth(label) + 4;
        }

        private boolean isOverHeader(int mx, int my) {
            int bx = boxX();
            return mx >= bx && mx < bx + dw && my >= ay() && my < ay() + HEIGHT;
        }

        private boolean isOverRow(int mx, int my, int i) {
            int bx  = boxX();
            int top = ay() + HEIGHT + 1 + i * ROW_H;
            return mx >= bx && mx < bx + dw && my >= top && my < top + ROW_H;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GLScrollPane
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Clipped scroll pane — add child GLWidgets to it.
     * Children's rx/ry are relative to the pane's own top-left.
     *
     * Usage:
     *   GLScrollPane pane = window.addScrollPane(4, 2, 190, 70);
     *   pane.addLabel("Item 1", 0, 0);
     *   pane.addLabel("Item 2", 0, 12);
     *   pane.setContentHeight(64);
     */
    public static class GLScrollPane extends GLWidget {
        private static final int SCROLLBAR_W = 4;

        private final int pw, ph;
        private int contentHeight;
        private int scrollY = 0;
        private final List<GLWidget> children = new ArrayList<>();

        // Scrollbar drag
        private boolean sbDragging = false;
        private int sbDragStartY;
        private int sbDragStartScroll;

        public GLScrollPane(GLWindow win, int rx, int ry, int pw, int ph) {
            super(win, rx, ry);
            this.pw = pw;
            this.ph = ph;
            this.contentHeight = ph; // default — no scroll
        }

        public void setContentHeight(int h) {
            this.contentHeight = h;
            clampScroll();
        }
        public int getScrollY() { return scrollY; }

        // ── Pane-local widget adders ────────────────────────────────────────────
        public GLLabel addLabel(String text, int rx, int ry) {
            GLLabel w = new PaneLabel(this, text, rx, ry);
            children.add(w);
            return w;
        }

        public GLButton addButton(String label, int rx, int ry, int bw, int bh, Runnable onClick) {
            GLButton w = new PaneButton(this, label, rx, ry, bw, bh, onClick);
            children.add(w);
            return w;
        }

        public GLCheckbox addCheckbox(String label, int rx, int ry,
                                      boolean checked, Consumer<Boolean> onChange) {
            GLCheckbox w = new PaneCheckbox(this, label, rx, ry, checked, onChange);
            children.add(w);
            return w;
        }

        // ── Absolute pane origin on screen ─────────────────────────────────────
        int paneAX() { return win.bodyX() + rx; }
        int paneAY() { return win.bodyY() + ry; }

        // ── Input ───────────────────────────────────────────────────────────────
        @Override
        public boolean mouseClicked(int mx, int my) {
            if (!isOverPane(mx, my)) return false;

            // Scrollbar?
            if (isOverScrollbar(mx, my)) {
                sbDragging = true;
                sbDragStartY = my;
                sbDragStartScroll = scrollY;
                return true;
            }

            // Pass to children
            for (int i = children.size() - 1; i >= 0; i--) {
                if (children.get(i).mouseClicked(mx, my)) return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double dy) {
            // 1. Hol den Skalierungsfaktor (z.B. 2.0 oder 3.0)
            double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

            // 2. Skaliere die Maus-Koordinaten RUNTER auf Minecraft-Niveau
            double scaledMX = mx / scale;
            double scaledMY = my / scale;

            // 3. Jetzt passen diese Werte zu rx/ry und win.bodyX()
            double absolutePanelX = win.bodyX() + rx;
            double absolutePanelY = win.bodyY() + ry;

            if (scaledMX >= absolutePanelX && scaledMX <= absolutePanelX + pw &&
                    scaledMY >= absolutePanelY && scaledMY <= absolutePanelY + ph) {

                scrollY -= (int)(dy * 8);
                clampScroll();
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int btn, double ddx, double ddy) {
            if (!sbDragging) return false;

            int scrollRange = contentHeight - ph;
            if (scrollRange <= 0) return true;

            int sbTrack = ph - scrollbarThumbH() - 2;
            float ratio = (float) scrollRange / sbTrack;
            scrollY = sbDragStartScroll + (int) ((my - sbDragStartY) * ratio);
            clampScroll();
            return true;
        }

        private void clampScroll() {
            scrollY = Math.max(0, Math.min(contentHeight - ph, scrollY));
        }

        public void releaseScrollbar() { sbDragging = false; }

        // ── Render ───────────────────────────────────────────────────────────────
        @Override
        public void render(DrawContext ctx, int mx, int my) {
            int ox = paneAX();
            int oy = paneAY();

            // Pane border
            fill(ctx, ox - 1, oy - 1, pw + 2, 1, COL_WIDGET_BORDER);
            fill(ctx, ox - 1, oy + ph, pw + 2, 1, COL_WIDGET_BORDER);
            fill(ctx, ox - 1, oy, 1, ph, COL_WIDGET_BORDER);
            fill(ctx, ox + pw, oy, 1, ph, COL_WIDGET_BORDER);

            // Clip children
            ctx.enableScissor(ox, oy, ox + pw - SCROLLBAR_W, oy + ph);

            for (GLWidget w : children) w.render(ctx, mx, my);

            ctx.disableScissor();

            // Scrollbar track
            int sbX = ox + pw - SCROLLBAR_W;
            fill(ctx, sbX, oy, SCROLLBAR_W, ph, COL_WIDGET_BG);

            // Scrollbar thumb
            if (contentHeight > ph) {
                int thumbH = scrollbarThumbH();
                int track = ph - thumbH;
                int thumbY = oy + (int) ((float) scrollY / (contentHeight - ph) * track);
                fill(ctx, sbX + 1, thumbY, SCROLLBAR_W - 2, thumbH, COL_ACCENT);
            }
        }

        private int scrollbarThumbH() {
            return Math.max(12, ph * ph / Math.max(1, contentHeight));
        }

        private boolean isOverPane(int mx, int my) {
            return mx >= paneAX() && mx < paneAX() + pw
                    && my >= paneAY() && my < paneAY() + ph;
        }

        private boolean isOverScrollbar(int mx, int my) {
            int sbX = paneAX() + pw - SCROLLBAR_W;
            return mx >= sbX && mx < sbX + SCROLLBAR_W
                    && my >= paneAY() && my < paneAY() + ph;
        }

        // ── Pane-local widget wrappers ──────────────────────────────────────────
        private static class PaneLabel extends GLLabel {
            private final GLScrollPane pane;
            PaneLabel(GLScrollPane pane, String text, int rx, int ry) {
                super(pane.win, text, rx, ry);
                this.pane = pane;
            }
            @Override protected int ax() { return pane.paneAX() + rx; }
            @Override protected int ay() { return pane.paneAY() + ry - pane.scrollY; }
        }

        private static class PaneButton extends GLButton {
            private final GLScrollPane pane;
            PaneButton(GLScrollPane pane, String label, int rx, int ry, int bw, int bh, Runnable onClick) {
                super(pane.win, label, rx, ry, bw, bh, onClick);
                this.pane = pane;
            }
            @Override protected int ax() { return pane.paneAX() + rx; }
            @Override protected int ay() { return pane.paneAY() + ry - pane.scrollY; }
        }

        private static class PaneCheckbox extends GLCheckbox {
            private final GLScrollPane pane;
            PaneCheckbox(GLScrollPane pane, String label, int rx, int ry,
                         boolean checked, Consumer<Boolean> onChange) {
                super(pane.win, label, rx, ry, checked, onChange);
                this.pane = pane;
            }
            @Override protected int ax() { return pane.paneAX() + rx; }
            @Override protected int ay() { return pane.paneAY() + ry - pane.scrollY; }
        }
    }
}