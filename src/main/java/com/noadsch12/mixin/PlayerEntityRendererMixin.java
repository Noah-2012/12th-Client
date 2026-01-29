package com.noadsch12.mixin;

import com.noadsch12.networking.ClientUserManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class PlayerEntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Unique
    private static final Identifier ICON = Identifier.of("12th-client", "logo.png");

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderClientIcon(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        // Ensure we are dealing with a player
        if (state instanceof PlayerEntityRenderState playerState && playerState.displayName != null) {

            // Fallback: If UUID field is truly gone, we use the display name string for the lookup
            // or you can check if ClientUserManager.USERS contains the player name.
            // For now, let's assume you'll use the ID logic from the second Mixin below.
            if (isClientUser(playerState)) {

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                float textWidth = textRenderer.getWidth(playerState.displayName);
                float iconSize = 8.0f;
                float padding = 2.0f;

                float iconWidth = 8.0f;
                float iconHeight = iconWidth * (738.0f / 865.0f);


                matrices.push();

                matrices.translate(-((textWidth + iconSize + padding) / 2.0f), 0f, 0f);

                queue.submitCustom(matrices, RenderLayer.getEntityTranslucent(ICON), (entry, buffer) -> {
                    Matrix4f matrix = entry.getPositionMatrix();

                    // Using 0 to 1 for textures maps the whole 865x738 image to the quad
                    buffer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).texture(0, 0).light(15728880).normal(0f, 0f, 1f);
                    buffer.vertex(matrix, iconWidth, 0, 0).color(255, 255, 255, 255).texture(1, 0).light(15728880).normal(0f, 0f, 1f);
                    buffer.vertex(matrix, iconWidth, iconHeight, 0).color(255, 255, 255, 255).texture(1, 1).light(15728880).normal(0f, 0f, 1f);
                    buffer.vertex(matrix, 0, iconHeight, 0).color(255, 255, 255, 255).texture(0, 1).light(15728880).normal(0f, 0f, 1f);
                });

                matrices.translate(iconSize + padding, 0f, 0f);
            }
        }
    }

    @Unique
    private boolean isClientUser(PlayerEntityRenderState state) {
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Direct check: Is this the local player (you)?
        if (client.player != null && state.id == client.player.getId()) {
            return true;
        }

        // 2. Fallback check for other players using your captured name list
        if (state.displayName != null) {
            String name = state.displayName.getString();
            return ClientUserManager.USERS_NAMES.contains(name);
        }

        return false;
    }
}