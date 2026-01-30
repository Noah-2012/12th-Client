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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ObjWorldHudRenderer {

    /**
     * Rendert ein OBJ-Modell an einer Welt-Position auf das HUD.
     */
    public static void renderAtWorldPos(DrawContext context, ObjModel model, Vec3d targetPos,
                                        Vec3d camPos, Matrix4f viewMat, Matrix4f projMat,
                                        int sw, int sh, float scale, int color) {
        if (model == null) return;

        // 1. Relativen Vektor zur Kamera berechnen (World -> Camera Space)
        float dx = (float) (targetPos.x - camPos.x);
        float dy = (float) (targetPos.y - camPos.y);
        float dz = (float) (targetPos.z - camPos.z);

        // 2. Jede Linie des Modells einzeln projizieren
        for (int[] face : model.faces) {
            for (int i = 0; i < face.length; i++) {
                Vector3f v1 = model.vertices.get(face[i]);
                Vector3f v2 = model.vertices.get(face[(i + 1) % face.length]);

                // Punkte projizieren
                Vector2f p1 = project(v1, dx, dy, dz, viewMat, projMat, sw, sh, scale);
                Vector2f p2 = project(v2, dx, dy, dz, viewMat, projMat, sw, sh, scale);

                // Nur zeichnen, wenn beide Punkte vor der Kamera sind
                if (p1 != null && p2 != null) {
                    drawLine(context, p1.x, p1.y, p2.x, p2.y, color);
                }
            }
        }
    }

    private static Vector2f project(Vector3f vertex, float dx, float dy, float dz,
                                    Matrix4f view, Matrix4f proj, int sw, int sh, float scale) {
        // Vertex-Position im Modell mit Skalierung + Welt-Offset
        Vector4f pos = new Vector4f(
                dx + (vertex.x * scale),
                dy + (vertex.y * scale),
                dz + (vertex.z * scale),
                1.0f
        );

        // Transformation: Camera -> Projection Space
        pos.mul(view).mul(proj);

        // Clip-Check: Hinter der Kamera?
        if (pos.w <= 0) return null;

        // NDC -> Screen Space
        float screenX = ((pos.x / pos.w) + 1.0f) * sw / 2.0f;
        float screenY = (1.0f - (pos.y / pos.w)) * sh / 2.0f;

        return new Vector2f(screenX, screenY);
    }

    private static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);

        var stack = context.getMatrices();
        stack.pushMatrix(); // Deine gespeicherte Methode
        stack.translate(x1, y1);
        stack.rotate(angle);

        // Zeichnet ein Rechteck mit Länge der Distanz und Breite 1 (die Linie)
        context.fill(0, 0, (int) dist, 1, color);

        stack.popMatrix(); // Deine gespeicherte Methode
    }

    // Hilfsklasse für 2D Koordinaten
    private static class Vector2f {
        public final float x, y;
        public Vector2f(float x, float y) { this.x = x; this.y = y; }
    }
}