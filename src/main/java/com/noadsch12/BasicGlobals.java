package com.noadsch12;

public class BasicGlobals {
    public static int SPACEBETWEENBUTTONS = 4; // in Pixels
    public static int MENUBUTTONSIZE = 200; // in Pixels

    public static int getButtonMiddleX(int ScreenW, int ButtonW) {
        return (ScreenW / 2) - (ButtonW / 2);
    }
}
