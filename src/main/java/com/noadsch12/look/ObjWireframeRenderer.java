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

package com.noadsch12.look;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ObjWireframeRenderer {

    public static void render(ObjModel model, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d cameraPos, float r, float g, float b) {
        if (model == null) return;

        // Use the custom push logic
        matrices.push();

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (int[] face : model.faces) {
            for (int i = 0; i < face.length; i++) {
                // Get current vertex and the next one to form a line
                Vector3f v1 = model.vertices.get(face[i]);
                Vector3f v2 = model.vertices.get(face[(i + 1) % face.length]);

                // Adjust for camera position (World Space to Camera Space)
                float x1 = (float) (v1.x - cameraPos.x);
                float y1 = (float) (v1.y - cameraPos.y);
                float z1 = (float) (v1.z - cameraPos.z);

                float x2 = (float) (v2.x - cameraPos.x);
                float y2 = (float) (v2.y - cameraPos.y);
                float z2 = (float) (v2.z - cameraPos.z);

                line(buffer, matrix, x1, y1, z1, x2, y2, z2, r, g, b);
            }
        }

        // Use the custom pop logic
        matrices.pop();
    }

    private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, 1f).normal(0, 1, 0);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, 1f).normal(0, 1, 0);
    }

            /*

        WorldRenderEvents.END_MAIN.register(context -> {
            ensureModelLoaded();
            Vec3d camPos = context.gameRenderer().getCamera().getPos();
            MatrixStack stack = context.matrices();

            // Example: Render at world origin (0, 70, 0)
            stack.push();
            stack.translate(0, 70, 0); // Note: standard translate uses x, y, z

            ObjWireframeRenderer.render(myLoadedModel, stack, context.consumers(), camPos, 1.0f, 1.0f, 1.0f);

            stack.pop();
        });

         */
}