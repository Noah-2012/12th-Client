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

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BlurHandler {

    public static void executeBlur(DrawContext context, int width, int height, float size) {
        var owoContext = OwoUIDrawContext.of(context);

        // 1. Push the screen area to the scissor stack
        ScreenRect rect = new ScreenRect(0, 0, width, height);
        owoContext.scissorStack.push(rect);

        // 2. Use a minimal implementation of ParentComponent
        Surface.blur(3.0f, size).draw(owoContext, new ParentComponent() {
            @Override public int x() { return 0; }

            @Override
            public void updateX(int i) {

            }

            @Override public int y() { return 0; }

            @Override
            public void updateY(int i) {

            }

            @Override
            public void draw(OwoUIDrawContext owoUIDrawContext, int i, int i1, float v, float v1) {

            }

            @Override
            public @Nullable ParentComponent parent() {
                return null;
            }

            @Override
            public @Nullable FocusHandler focusHandler() {
                return null;
            }

            @Override
            public Component positioning(Positioning positioning) {
                return null;
            }

            @Override
            public AnimatableProperty<Positioning> positioning() {
                return null;
            }

            @Override
            public Component margins(Insets insets) {
                return null;
            }

            @Override
            public AnimatableProperty<Insets> margins() {
                return null;
            }

            @Override
            public Component horizontalSizing(Sizing sizing) {
                return null;
            }

            @Override
            public AnimatableProperty<Sizing> horizontalSizing() {
                return null;
            }

            @Override
            public Component verticalSizing(Sizing sizing) {
                return null;
            }

            @Override
            public AnimatableProperty<Sizing> verticalSizing() {
                return null;
            }

            @Override
            public Component id(@Nullable String s) {
                return null;
            }

            @Override
            public @Nullable String id() {
                return "";
            }

            @Override
            public Component tooltip(@Nullable List<TooltipComponent> list) {
                return null;
            }

            @Override
            public @Nullable List<TooltipComponent> tooltip() {
                return List.of();
            }

            @Override
            public void inflate(Size size) {

            }

            @Override
            public void mount(ParentComponent parentComponent, int i, int i1) {

            }

            @Override
            public void dismount(DismountReason dismountReason) {

            }

            @Override
            public <C extends Component> C configure(Consumer<C> consumer) {
                return null;
            }

            @Override
            public EventSource<MouseDown> mouseDown() {
                return null;
            }

            @Override
            public boolean onMouseUp(Click click) {
                return false;
            }

            @Override
            public EventSource<MouseUp> mouseUp() {
                return null;
            }

            @Override
            public EventSource<MouseScroll> mouseScroll() {
                return null;
            }

            @Override
            public boolean onMouseDrag(Click click, double v, double v1) {
                return false;
            }

            @Override
            public EventSource<MouseDrag> mouseDrag() {
                return null;
            }

            @Override
            public boolean onKeyPress(KeyInput keyInput) {
                return false;
            }

            @Override
            public EventSource<KeyPress> keyPress() {
                return null;
            }

            @Override
            public boolean onCharTyped(CharInput charInput) {
                return false;
            }

            @Override
            public EventSource<CharTyped> charTyped() {
                return null;
            }

            @Override
            public void onFocusGained(FocusSource focusSource) {

            }

            @Override
            public EventSource<FocusGained> focusGained() {
                return null;
            }

            @Override
            public void onFocusLost() {

            }

            @Override
            public EventSource<FocusLost> focusLost() {
                return null;
            }

            @Override
            public EventSource<MouseEnter> mouseEnter() {
                return null;
            }

            @Override
            public EventSource<MouseLeave> mouseLeave() {
                return null;
            }

            @Override
            public CursorStyle cursorStyle() {
                return null;
            }

            @Override
            public Component cursorStyle(CursorStyle cursorStyle) {
                return null;
            }

            @Override public int width() { return width; }
            @Override public int height() { return height; }

            // Minimal interface requirements
            @Override public void layout(Size size) {}

            @Override
            public void onChildMutated(Component component) {

            }

            @Override
            public void queue(Runnable runnable) {

            }

            @Override
            public ParentComponent verticalAlignment(VerticalAlignment verticalAlignment) {
                return null;
            }

            @Override
            public VerticalAlignment verticalAlignment() {
                return null;
            }

            @Override
            public ParentComponent horizontalAlignment(HorizontalAlignment horizontalAlignment) {
                return null;
            }

            @Override
            public HorizontalAlignment horizontalAlignment() {
                return null;
            }

            @Override
            public ParentComponent padding(Insets insets) {
                return null;
            }

            @Override
            public AnimatableProperty<Insets> padding() {
                return null;
            }

            @Override
            public ParentComponent allowOverflow(boolean b) {
                return null;
            }

            @Override
            public boolean allowOverflow() {
                return false;
            }

            @Override
            public ParentComponent surface(Surface surface) {
                return null;
            }

            @Override
            public Surface surface() {
                return null;
            }

            @Override
            public ParentComponent removeChild(Component component) {
                return null;
            }

            @Override
            public List<Component> children() {
                throw new UnsupportedOperationException("Unimplemented method 'children'");
            }
        });

        // 3. Force the shader to flush onto the screen
        owoContext.fill(0, 0, width, height, 0x05000000);

        // 4. Pop the scissor
        owoContext.scissorStack.pop();
    }
}