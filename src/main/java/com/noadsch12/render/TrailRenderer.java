package com.noadsch12.render;

import com.noadsch12.ui.screens.ClientSettingsScreen;
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
        Matrix4f matrix = matrices.peek().getPositionMatrix();

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

                if (ClientSettingsScreen.trailColorIndex == 0) {
                    lines.vertex(positionMatrix, x1, y1, z1).color(255, 0, 0, 255).normal(0, 1, 0);
                    lines.vertex(positionMatrix, x2, y2, z2).color(255, 0, 0, 255).normal(0, 1, 0);
                } else if (ClientSettingsScreen.trailColorIndex == 1) {
                    lines.vertex(positionMatrix, x1, y1, z1).color(0, 0, 255, 255).normal(0, 1, 0);
                    lines.vertex(positionMatrix, x2, y2, z2).color(0, 0, 255, 255).normal(0, 1, 0);
                } else if (ClientSettingsScreen.trailColorIndex == 2) {
                    lines.vertex(positionMatrix, x1, y1, z1).color(0, 255, 0, 255).normal(0, 1, 0);
                    lines.vertex(positionMatrix, x2, y2, z2).color(0, 255, 0, 255).normal(0, 1, 0);
                } else if (ClientSettingsScreen.trailColorIndex == 3) {
                    lines.vertex(positionMatrix, x1, y1, z1).color(255, 255, 255, 255).normal(0, 1, 0);
                    lines.vertex(positionMatrix, x2, y2, z2).color(255, 255, 255, 255).normal(0, 1, 0);
                } else if (ClientSettingsScreen.trailColorIndex == 4) {
                    lines.vertex(positionMatrix, x1, y1, z1).color(0, 0, 0, 255).normal(0, 1, 0);
                    lines.vertex(positionMatrix, x2, y2, z2).color(0, 0, 0, 255).normal(0, 1, 0);
                }
            }
        }

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(RenderLayer.getLines());
        }
    }
}