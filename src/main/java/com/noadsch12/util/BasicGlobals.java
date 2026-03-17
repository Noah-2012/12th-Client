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

package com.noadsch12.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class BasicGlobals {
    public static int SPACEBETWEENBUTTONS = 4; // in Pixels
    public static int MENUBUTTONSIZE = 200; // in Pixels
    public static final String CLIENT_VERSION = "1.1.3";

    public static final Identifier ARIAL_FONT = Identifier.of("12th-client", "consolas");
    public static final Identifier GOTHIC_FONT = Identifier.of("12th-client", "gothic");

    public static int getButtonMiddleX(int ScreenW, int ButtonW) {
        return (ScreenW / 2) - (ButtonW / 2);
    }

    public int getMaxFps(MinecraftClient mc) {
        return mc.options.getMaxFps().getValue();
    }
}
