package com.noadsch12.mixin;

import com.noadsch12.util.CursorState;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyArg(
            method = "setTitle",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowTitle(JLjava/lang/CharSequence;)V"
            ),
            index = 1
    )
    private CharSequence replaceTitle(CharSequence original) {
        return "12th Client (v1.1.3)";
    }
}
