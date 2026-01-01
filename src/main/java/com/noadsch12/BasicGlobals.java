package com.noadsch12;

public class BasicGlobals {
    public static int SPACEBETWEENBUTTONS = 4; // in Pixels
    public static int MENUBUTTONSIZE = 200; // in Pixels
    public static final String CLIENT_VERSION = "1.1.3";

    public static int getButtonMiddleX(int ScreenW, int ButtonW) {
        return (ScreenW / 2) - (ButtonW / 2);
    }
}
