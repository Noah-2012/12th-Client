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

import com.noadsch12.ui.GLWindow;

import java.util.List;

public class DemoWindowFactory {

    public static GLWindow createDemoWindow() {

        GLWindow win = new GLWindow("GLWindow Demo", 40, 40);

        int y = 4;

        // ─────────────────────────────────────
        // Label
        // ─────────────────────────────────────
        win.addLabel("This is a label", 4, y);
        y += 10;

        // Separator
        win.addSeparator(y);
        y += 6;

        // ─────────────────────────────────────
        // Button
        // ─────────────────────────────────────
        win.addButton("Click me", 4, y, 80, 12,
                () -> System.out.println("Button clicked"));
        y += 16;

        // ─────────────────────────────────────
        // Checkbox
        // ─────────────────────────────────────
        win.addCheckbox("Enable feature", 4, y, false,
                state -> System.out.println("Checkbox: " + state));
        y += 14;

        // ─────────────────────────────────────
        // Text field
        // ─────────────────────────────────────
        win.addTextField(4, y, 120, "Type here...",
                text -> System.out.println("Text: " + text));
        y += 16;

        // ─────────────────────────────────────
        // Slider
        // ─────────────────────────────────────
        win.addSlider("Volume", 4, y, 120,
                0, 100, 50,
                value -> System.out.println("Slider: " + value));
        y += 22;

        // ─────────────────────────────────────
        // Dropdown
        // ─────────────────────────────────────
        win.addDropdown(
                "Mode",
                4, y,
                100,
                List.of("Option A", "Option B", "Option C"),
                0,
                index -> System.out.println("Dropdown: " + index)
        );
        y += 18;

        // ─────────────────────────────────────
        // Separator
        // ─────────────────────────────────────
        win.addSeparator(y);
        y += 6;

        // ─────────────────────────────────────
        // Scroll pane demo
        // ─────────────────────────────────────
        GLWindow.GLScrollPane pane = win.addScrollPane(4, y, 180, 60);

        int py = 0;

        for (int i = 0; i < 20; i++) {
            int id = i;
            pane.addLabel("Scrollable label " + i, 2, py);
            py += 12;

            pane.addButton("Button " + i, 2, py, 70, 11,
                    () -> System.out.println("Pane button " + id));
            py += 14;

            pane.addCheckbox("Check " + i, 80, py - 12, false,
                    v -> System.out.println("Pane checkbox " + id + ": " + v));
        }

        pane.setContentHeight(py + 500);

        return win;
    }
}
