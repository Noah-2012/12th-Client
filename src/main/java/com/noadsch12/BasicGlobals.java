package com.noadsch12;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class BasicGlobals {
    public static int SPACEBETWEENBUTTONS = 4; // in Pixels
    public static int MENUBUTTONSIZE = 200; // in Pixels
    public static final String CLIENT_VERSION = "1.1.3";

    public static final Identifier ARIAL_FONT = Identifier.of("12th-client", "arial");

    private static MinecraftClient mc;

    public static int getButtonMiddleX(int ScreenW, int ButtonW) {
        return (ScreenW / 2) - (ButtonW / 2);
    }

    public int getMaxFps(MinecraftClient mc) {
        return mc.options.getMaxFps().getValue();
    }
}
