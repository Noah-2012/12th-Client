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

package com.noadsch12.render.fx;

import com.noadsch12.modules.impl.render.TrailSettings;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.*;

public class TrailRenderer {
    private static final Map<UUID, List<Vec3d>> trails = new HashMap<>();
    private static final int MAX_POINTS = 30;

    public static void addPoint(UUID id, Vec3d pos) {
        trails.computeIfAbsent(id, k -> new ArrayList<>()).add(pos);
        if (trails.get(id).size() > MAX_POINTS) {
            trails.get(id).removeFirst();
        }
    }

    // Notice we changed the parameters to match your working EntityESP.render()
    public static void render(Camera camera, VertexConsumerProvider vertexConsumers, Matrix4f positionMatrix) {

        Vec3d cameraPos = camera.getPos();

        // We create a new MatrixStack and apply the positionMatrix just like your ESP
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);

        // We get the lines buffer - this handles the Shader and Culling for us!
        VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.getLines());

        for (List<Vec3d> points : trails.values()) {
            for (int i = 0; i < points.size() - 1; i++) {
                Vec3d start = points.get(i);
                Vec3d end = points.get(i + 1);

                // Draw the line using the same helper logic as your ESP
                //drawLine(lines, matrix, start, end, cameraPos, 1f, 0f, 0f);
                float x1 = (float)(start.x - cameraPos.x);
                float y1 = (float)(start.y - cameraPos.y);
                float z1 = (float)(start.z - cameraPos.z);
                float x2 = (float)(end.x - cameraPos.x);
                float y2 = (float)(end.y - cameraPos.y);
                float z2 = (float)(end.z - cameraPos.z);

                int[] rgb = TrailSettings.getCurrentColorRGB();
                lines.vertex(positionMatrix, x1, y1, z1).color(rgb[0], rgb[1], rgb[2], 255).normal(0, 1, 0);
                lines.vertex(positionMatrix, x2, y2, z2).color(rgb[0], rgb[1], rgb[2], 255).normal(0, 1, 0);
            }
        }

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(RenderLayer.getLines());
        }
    }
}