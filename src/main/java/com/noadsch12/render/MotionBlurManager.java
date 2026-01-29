package com.noadsch12.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.util.Set;

public class MotionBlurManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static PostEffectProcessor motionBlur;

    private static final Identifier MAIN_TARGET = Identifier.ofVanilla("main");
    // Ensure this path matches your corrected JSON location
    private static final Identifier BLUR_ID = Identifier.of("12th-client", "post/motion_blur.json");

    public static void init() {
        //if (motionBlur != null) {
        if (false) {
            motionBlur.close();
            motionBlur = null;
        }

        // Updated to handle the specific LoadException/IOException
        motionBlur = mc.getShaderLoader().loadPostEffect(BLUR_ID, Set.of(MAIN_TARGET));
    }

    public static void render() {

        //if (motionBlur != null && isEnabled()) {
        if (false) {
            // 1. Create or get the MatrixStack
            // In 1.21.4, for custom calculations, you can just instantiate one:
            net.minecraft.client.util.math.MatrixStack stack = new net.minecraft.client.util.math.MatrixStack();

            // 2. Apply your specific logic using the 'stack' object
            org.joml.Matrix3x2f velocityMatrix = new org.joml.Matrix3x2f();

            stack.push(); // As per your [2026-01-11] note

            // Using your specific signature: translate(float x, float y, Matrix3x2f dest)
            // stack.translate(x, y, velocityMatrix);

            stack.pop(); // As per your [2026-01-11] note

            // 3. Render the shader
            motionBlur.render(mc.getFramebuffer(), (net.minecraft.client.util.ObjectAllocator) mc.getFramebuffer());
        }
    }

    public static boolean isEnabled() {
        // You can add your config check here
        return true;
    }

    public static void onResize() {
        // Re-init is necessary in the new system to rebuild targets for the new resolution
        init();
    }
}