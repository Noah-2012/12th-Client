package com.noadsch12.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.noadsch12.render.entity.EntityESP;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class EntityRenderManagerMixin {

    @Shadow @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(ObjectAllocator allocator, RenderTickCounter tickCounter,
                             boolean renderBlockOutline, Camera camera,
                             Matrix4f positionMatrix, Matrix4f viewMatrix,
                             Matrix4f projectionMatrix, GpuBufferSlice fogBuffer,
                             Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (ClientSettingsScreen.EntityEspEnabled) {
            EntityESP.render(camera, bufferBuilders.getEntityVertexConsumers(), positionMatrix);
        }
    }
}