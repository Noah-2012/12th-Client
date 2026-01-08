package com.noadsch12.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.noadsch12.render.TrailRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // This shadow gives us access to the game's main vertex consumers
    @Shadow private BufferBuilderStorage bufferBuilders;

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        // Use the bufferBuilders shadow to get the entity consumers
        VertexConsumerProvider consumers = this.bufferBuilders.getEntityVertexConsumers();

        // Pass the arguments to your working TrailRenderer logic
        TrailRenderer.render(camera, consumers, positionMatrix);
    }
}