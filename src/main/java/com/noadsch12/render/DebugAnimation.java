package com.noadsch12.render;

public class DebugAnimation {
    public static float progress = 0f;
    public static boolean isF3Visible = false;
    public static boolean hasBeenActivated = false;

    private static long lastTime = System.currentTimeMillis();
    private static final float SPEED = 4.0f; // 4.0f = 0.25 Sekunden für die Animation

    public static void update() {
        long currentTime = System.currentTimeMillis();
        // Zeit seit dem letzten Frame in Sekunden (z.B. 0.016 für 60 FPS)
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        if (isF3Visible) {
            progress = Math.min(1f, progress + SPEED * deltaTime);
        } else {
            progress = Math.max(0f, progress - SPEED * deltaTime);
        }
    }

    public static boolean shouldActuallyRender() {
        return isF3Visible || progress > 0.001f;
    }

    public static float getEasedProgress() {
        return (float) Math.sin(progress * Math.PI / 2);
    }
}
