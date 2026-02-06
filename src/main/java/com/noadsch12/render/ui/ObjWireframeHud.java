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

package com.noadsch12.render.ui;

import com.noadsch12.look.ObjModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ObjWireframeHud {

    private static final int THIN_WHITE = 0x99FFFFFF;
    // Geschwindigkeit der Rotation (höherer Wert = schneller)
    private static final float ROTATION_SPEED = 0.002f;

    public static void render(DrawContext context, ObjModel model, int x, int y, float scale) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        // Nutze Millisekunden für absolut flüssige Bewegung unabhängig von Ticks
        long currentTime = Util.getMeasuringTimeMs();
        float rotationAngle = currentTime * ROTATION_SPEED;

        // 3D-Transformation vorbereiten
        Matrix4f modelMatrix = new Matrix4f()
                .rotateX((float) Math.toRadians(MinecraftClient.getInstance().gameRenderer.getCamera().getPitch()))
                //.rotateY(rotationAngle);            // Drehung um die eigene Achse
                .rotateY((float) Math.toRadians(MinecraftClient.getInstance().gameRenderer.getCamera().getYaw()));

        var stack = context.getMatrices();
        stack.pushMatrix(); // Deine gespeicherte Methode

        // Position auf dem Bildschirm
        stack.translate((float)x, (float)y);

        for (int[] face : model.faces) {
            for (int i = 0; i < face.length; i++) {
                Vector3f v1Raw = model.vertices.get(face[i]);
                Vector3f v2Raw = model.vertices.get(face[(i + 1) % face.length]);

                // Manuelle Projektion der 3D-Punkte
                Vector4f v1 = new Vector4f(v1Raw.x, v1Raw.y, v1Raw.z, 1.0f).mul(modelMatrix);
                Vector4f v2 = new Vector4f(v2Raw.x, v2Raw.y, v2Raw.z, 1.0f).mul(modelMatrix);

                // Zeichnen der Linie mit deiner bewährten Tracer-Logik
                drawLine(context,
                        v1.x * scale, v1.y * scale,
                        v2.x * scale, v2.y * scale,
                        THIN_WHITE);
            }
        }

        stack.popMatrix(); // Deine gespeicherte Methode
    }

    private static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);

        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(x1, y1);
        stack.rotate(angle);

        context.fill(0, 0, (int) distance, 1, color);

        stack.popMatrix();
    }
}