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

package com.noadsch12.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniformStorage;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class aoba {
    public static class AobaRenderPipelines {

        public static final List<RenderPipeline> PIPELINES = new ArrayList<>();

        // 3D Pipelines
        public static final RenderPipeline QUADS = RenderPipelines.register(RenderPipeline.builder()
                .withVertexShader(Identifier.of("aoba", "shaders/pos_color.vert"))
                .withFragmentShader(Identifier.of("aoba", "shaders/pos_color.frag")).withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation("pipeline/aoba_quads").build());

        public static final RenderPipeline TRIS = RenderPipelines.register(RenderPipeline.builder()
                .withVertexShader(Identifier.of("aoba", "shaders/pos_color.vert"))
                .withFragmentShader(Identifier.of("aoba", "shaders/pos_color.frag")).withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation("pipeline/aoba_tris").build());

        public static final RenderPipeline LINES = RenderPipelines.register(RenderPipeline.builder()
                .withVertexShader(Identifier.of("12th-client", "shaders/pos_color.vert"))
                .withFragmentShader(Identifier.of("12th-client", "shaders/pos_color.frag")).withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.LINES)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation(Identifier.of("12th-client", "lines_pipeline")).build());

        // 2D Pipelines
        public static final RenderPipeline TRIS_GUI = addPipeline(RenderPipelines.register(RenderPipeline.builder()
                .withVertexShader(Identifier.of("aoba", "shaders/pos_color.vert"))
                .withFragmentShader(Identifier.of("aoba", "shaders/pos_color.frag")).withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES).withCull(false).withDepthWrite(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withLocation(Identifier.of("aoba", "pipeline/aoba_tris_gui")).build()));

        public static final RenderPipeline LINES_GUI = addPipeline(RenderPipelines
                .register(RenderPipeline.builder().withLocation(Identifier.of("aoba", "pipeline/aoba_lines_gui"))
                        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.LINES)
                        .withVertexShader(Identifier.of("aoba", "shaders/pos_color.vert"))
                        .withFragmentShader(Identifier.of("aoba", "shaders/pos_color.frag"))
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false)
                        .withBlend(BlendFunction.TRANSLUCENT).withCull(true).build()));

        public static RenderPipeline addPipeline(RenderPipeline pipeline) {
            PIPELINES.add(pipeline);
            return pipeline;
        }

        // Thanks Meteor! I needed this.
        public static void precompile() {
            GpuDevice device = RenderSystem.getDevice();
            ResourceManager resources = MinecraftClient.getInstance().getResourceManager();

            for (RenderPipeline pipeline : PIPELINES) {
                device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                    var resource = resources.getResource(identifier).get();

                    try (var in = resource.getInputStream()) {
                        return IOUtils.toString(in, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

    }

    public static class BufferManager {
        private static final int INITIAL_VERTEX_CAPACITY = 1024;
        private static final int INITIAL_INDEX_CAPACITY = 1024;

        private ByteBuffer vertexBuffer;
        private IntBuffer indexBuffer;
        private int vertexCount;
        private int indexCount;
        private final int vertexSize;

        public BufferManager(int vertexSize) {
            this.vertexSize = vertexSize;
            this.vertexBuffer = BufferUtils.createByteBuffer(INITIAL_VERTEX_CAPACITY * vertexSize);
            this.indexBuffer = BufferUtils.createIntBuffer(INITIAL_INDEX_CAPACITY);
            this.vertexCount = 0;
            this.indexCount = 0;
        }

        public void clear() {
            vertexBuffer.clear();
            indexBuffer.clear();
            vertexCount = 0;
            indexCount = 0;
        }

        public void ensureVertexCapacity(int additionalVertices) {
            int totalVertices = vertexCount + additionalVertices;
            int requiredBytes = totalVertices * vertexSize;

            if (requiredBytes > vertexBuffer.capacity()) {
                int newCapacity = Math.max(vertexBuffer.capacity() * 2, requiredBytes);
                ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
                vertexBuffer.flip();
                newBuffer.put(vertexBuffer);
                vertexBuffer = newBuffer;
            }
        }

        public void ensureIndexCapacity(int additionalIndices) {
            int totalIndices = indexCount + additionalIndices;

            if (totalIndices > indexBuffer.capacity()) {
                int newCapacity = Math.max(indexBuffer.capacity() * 2, totalIndices);
                IntBuffer newBuffer = BufferUtils.createIntBuffer(newCapacity);
                indexBuffer.flip();
                newBuffer.put(indexBuffer);
                indexBuffer = newBuffer;
            }
        }

        public void addVertex(float x, float y, float z) {
            ensureVertexCapacity(3);
            vertexBuffer.putFloat(x);
            vertexBuffer.putFloat(y);
            vertexBuffer.putFloat(z);
            vertexCount++;
        }

        public void addColor(Color color) {
            ensureVertexCapacity(1);
            vertexBuffer.put((byte) color.r);
            vertexBuffer.put((byte) color.g);
            vertexBuffer.put((byte) color.b);
            vertexBuffer.put((byte) color.a);
        }

        public void addIndex(int index) {
            ensureIndexCapacity(1);
            indexBuffer.put(index);
            indexCount++;
        }

        public void addTriangle(int v1, int v2, int v3) {
            ensureIndexCapacity(3);
            indexBuffer.put(v1);
            indexBuffer.put(v2);
            indexBuffer.put(v3);
            indexCount += 3;
        }

        public void addLine(int v1, int v2) {
            ensureIndexCapacity(2);
            indexBuffer.put(v1);
            indexBuffer.put(v2);
            indexCount += 2;
        }

        public GpuBuffer createVertexBuffer(VertexFormat vertexFormat) {
            if (vertexCount == 0) {
                throw new IllegalStateException("Cannot create vertex buffer with no vertices");
            }

            ByteBuffer vertexData = BufferUtils.createByteBuffer(vertexCount * vertexSize);
            vertexBuffer.flip();
            if (vertexBuffer.remaining() == 0) {
                throw new IllegalStateException("Vertex buffer is empty after flip, vertexCount=" + vertexCount);
            }
            vertexData.put(vertexBuffer);
            vertexData.flip();
            return vertexFormat.uploadImmediateVertexBuffer(vertexData);
        }

        public GpuBuffer createIndexBuffer(VertexFormat vertexFormat) {
            if (indexCount == 0) {
                throw new IllegalStateException("Cannot create index buffer with no indices");
            }

            ByteBuffer indexByteBuffer = BufferUtils.createByteBuffer(indexCount * 4);
            indexBuffer.flip();

            if (indexBuffer.remaining() == 0) {
                throw new IllegalStateException("Index buffer is empty after flip, indexCount=" + indexCount);
            }

            IntBuffer intView = indexByteBuffer.asIntBuffer();
            intView.put(indexBuffer);

            indexByteBuffer.position(0);
            indexByteBuffer.limit(indexCount * 4);
            return vertexFormat.uploadImmediateIndexBuffer(indexByteBuffer);
        }

        public int getVertexCount() {
            return vertexCount;
        }

        public int getIndexCount() {
            return indexCount;
        }

        public boolean isEmpty() {
            return indexCount == 0 || vertexCount == 0;
        }

        public void resetAfterRender() {
            vertexBuffer.clear();
            indexBuffer.clear();
            vertexCount = 0;
            indexCount = 0;
        }
    }

    public static interface IRenderer {
        void begin();

        void end();

        boolean isBuilding();

        void reset();

        void render();

        void clearStorageFrame();
    }

    public static class Rectangle {
        public static final Rectangle INFINITE = new Rectangle(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        private Float x = null;
        private Float y = null;
        private Float width = null;
        private Float height = null;

        public Rectangle() {
        }

        public Rectangle(Float x, Float y, Float width, Float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle(Rectangle rect) {
            x = rect.x;
            y = rect.y;
            width = rect.width;
            height = rect.height;
        }

        @Nullable
        public Float getX() {
            return x;
        }

        @Nullable
        public Float getY() {
            return y;
        }

        @Nullable
        public Float getWidth() {
            return width;
        }

        @Nullable
        public Float getHeight() {
            return height;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;

            Rectangle otherRect = (Rectangle) other;

            if (!Objects.equals(x, otherRect.x))
                return false;
            if (!Objects.equals(y, otherRect.y))
                return false;
            if (!Objects.equals(width, otherRect.width))
                return false;
            return Objects.equals(height, otherRect.height);
        }

        public boolean intersects(Rectangle rectangle) {
            return (Math.abs(x - rectangle.x) * 2 < (width + rectangle.width))
                    && (Math.abs(y - rectangle.y) * 2 < (height + rectangle.height));
        }

        public boolean intersects(float x, float y) {
            float x2 = this.x + width;
            float y2 = this.y + height;

            return (x >= this.x && x <= x2 && y >= this.y && y <= y2);
        }

        public void setX(Float x) {
            this.x = x;
        }

        public void setY(Float y) {
            this.y = y;
        }

        public void setWidth(Float width) {
            this.width = width;
        }

        public void setHeight(Float height) {
            this.height = height;
        }

        /**
         * Returns whether or not this rectangle can be used for rendering, such that
         * the X, Y, Width, and Height dimensions are all non-null.
         *
         * @return Whether this rectangle can be used for rendering.
         */
        public boolean isDrawable() {
            return !(x == null || y == null || width == null || height == null);
        }
    }

    public static class Render2D implements IRenderer {
        private final DynamicUniformStorage<UboData> UNIFORM_STORAGE = new DynamicUniformStorage<>("Aoba 2D UBO",
                new Std140SizeCalculator().putMat4f().putMat4f().get(), 16);
        private final UboData UBO_DATA = new UboData();

        private final BufferManager triangleBuffer;
        private final BufferManager lineBuffer;
        private boolean isBuilding = false;
        private int currentVertexIndex = 0;

        public Render2D() {
            this.triangleBuffer = new BufferManager(28);
            this.lineBuffer = new BufferManager(VertexFormats.POSITION_COLOR.getVertexSize());
        }

        @Override
        public void begin() {
            if (isBuilding) {
                throw new IllegalStateException("Renderer is already building");
            }

            isBuilding = true;
            triangleBuffer.resetAfterRender();
            lineBuffer.resetAfterRender();
            currentVertexIndex = 0;
        }

        @Override
        public void end() {
            if (!isBuilding) {
                throw new IllegalStateException("Renderer is not building");
            }
            isBuilding = false;
        }

        @Override
        public boolean isBuilding() {
            return isBuilding;
        }

        @Override
        public void reset() {
            triangleBuffer.resetAfterRender();
            lineBuffer.resetAfterRender();
            currentVertexIndex = 0;
        }

        @Override
        public void render() {
            System.out.println("2D Render called - triangles: v=" + triangleBuffer.getVertexCount() + " i="
                    + triangleBuffer.getIndexCount() + ", lines: v=" + lineBuffer.getVertexCount() + " i="
                    + lineBuffer.getIndexCount());
            try {
                if (!triangleBuffer.isEmpty()) {
                    renderBuffer(triangleBuffer, AobaRenderPipelines.TRIS_GUI);
                }
                if (!lineBuffer.isEmpty()) {
                    renderBuffer(lineBuffer, AobaRenderPipelines.LINES_GUI);
                }
            } finally {
                reset();
            }
        }

        private void renderBuffer(BufferManager buffer, RenderPipeline pipeline) {
            if (buffer.getVertexCount() == 0 || buffer.getIndexCount() == 0) {
                return;
            }

            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

            GpuTextureView colorAttachment = framebuffer.getColorAttachmentView();
            GpuBuffer vertexBuffer = buffer.createVertexBuffer(VertexFormats.POSITION_COLOR);
            GpuBuffer indexBuffer = buffer.createIndexBuffer(VertexFormats.POSITION_COLOR);


            UBO_DATA.proj = RenderManager.projection;
            UBO_DATA.modelView = RenderSystem.getModelViewMatrix();

            GpuBufferSlice matrixData = UNIFORM_STORAGE.write(UBO_DATA);

            RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Aoba 2D Renderer",
                    colorAttachment, OptionalInt.empty());

            pass.setPipeline(pipeline);
            pass.setUniform("Matrices", matrixData);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
            pass.drawIndexed(0, 0, buffer.getIndexCount(), 1);
            pass.close();
        }

        public void drawBox(float x, float y, float width, float height, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            int startVertex = currentVertexIndex;

            triangleBuffer.addVertex(x, y, 0);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex(x + width, y, 0);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex(x + width, y + height, 0);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex(x, y + height, 0);
            triangleBuffer.addColor(color);

            triangleBuffer.addTriangle(startVertex, startVertex + 1, startVertex + 2);
            triangleBuffer.addTriangle(startVertex, startVertex + 2, startVertex + 3);

            currentVertexIndex += 4;
        }

        public void drawBox(Rectangle rect, Color color) {
            drawBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color);
        }

        public void drawBoxOutline(float x, float y, float width, float height, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            int startVertex = currentVertexIndex;

            lineBuffer.addVertex(x, y, 0);
            lineBuffer.addColor(color);
            lineBuffer.addVertex(x + width, y, 0);
            lineBuffer.addColor(color);
            lineBuffer.addVertex(x + width, y + height, 0);
            lineBuffer.addColor(color);
            lineBuffer.addVertex(x, y + height, 0);
            lineBuffer.addColor(color);

            lineBuffer.addLine(startVertex, startVertex + 1);
            lineBuffer.addLine(startVertex + 1, startVertex + 2);
            lineBuffer.addLine(startVertex + 2, startVertex + 3);
            lineBuffer.addLine(startVertex + 3, startVertex);

            currentVertexIndex += 4;
        }

        public void drawBoxOutline(Rectangle rect, Color color) {
            drawBoxOutline(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color);
        }

        public void drawLine(float x1, float y1, float x2, float y2, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            int startVertex = currentVertexIndex;

            lineBuffer.addVertex(x1, y1, 0);
            lineBuffer.addColor(color);
            lineBuffer.addVertex(x2, y2, 0);
            lineBuffer.addColor(color);

            lineBuffer.addLine(startVertex, startVertex + 1);

            currentVertexIndex += 2;
        }

        public void drawRoundedBox(float x, float y, float width, float height, float radius, Color color) {
            if (radius <= 0) {
                drawBox(x, y, width, height, color);
                return;
            }

            drawBox(x + radius, y, width - 2 * radius, height, color);
            drawBox(x, y + radius, radius, height - 2 * radius, color);
            drawBox(x + width - radius, y + radius, radius, height - 2 * radius, color);

            drawFilledArc(x + radius, y + radius, radius, 180, 90, color);
            drawFilledArc(x + width - radius, y + radius, radius, 270, 90, color);
            drawFilledArc(x + width - radius, y + height - radius, radius, 0, 90, color);
            drawFilledArc(x + radius, y + height - radius, radius, 90, 90, color);
        }

        public void drawRoundedBox(Rectangle rect, float radius, Color color) {
            drawRoundedBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), radius, color);
        }

        public void drawCircle(float x, float y, float radius, Color color) {
            drawFilledArc(x, y, radius, 0, 360, color);
        }

        private void drawFilledArc(float centerX, float centerY, float radius, float startAngle, float arcAngle,
                                   Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            int segments = Math.max(8, (int) (arcAngle / 15.0f));
            float angleStep = arcAngle / segments;

            int centerVertex = currentVertexIndex;
            triangleBuffer.addVertex(centerX, centerY, 0);
            triangleBuffer.addColor(color);
            currentVertexIndex++;

            for (int i = 0; i <= segments; i++) {
                float angle = (float) Math.toRadians(startAngle + i * angleStep);
                float px = centerX + radius * (float) Math.cos(angle);
                float py = centerY + radius * (float) Math.sin(angle);

                triangleBuffer.addVertex(px, py, 0);
                triangleBuffer.addColor(color);

                if (i > 0) {
                    triangleBuffer.addTriangle(centerVertex, currentVertexIndex - 1, currentVertexIndex);
                }
                currentVertexIndex++;
            }
        }

        public void drawOutlinedBox(float x, float y, float width, float height, Color outlineColor, Color fillColor) {
            drawBox(x, y, width, height, fillColor);
            drawBoxOutline(x, y, width, height, outlineColor);
        }

        public void drawOutlinedBox(Rectangle rect, Color outlineColor, Color fillColor) {
            drawOutlinedBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), outlineColor, fillColor);
        }

        public void drawTriangle(float x1, float y1, Color color1, float x2, float y2, Color color2, float x3, float y3,
                                 Color color3) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            int startVertex = currentVertexIndex;

            triangleBuffer.addVertex(x1, y1, 0);
            triangleBuffer.addColor(color1);
            triangleBuffer.addVertex(x2, y2, 0);
            triangleBuffer.addColor(color2);
            triangleBuffer.addVertex(x3, y3, 0);
            triangleBuffer.addColor(color3);

            triangleBuffer.addTriangle(startVertex, startVertex + 1, startVertex + 2);

            currentVertexIndex += 3;
        }

        public void drawHorizontalGradient(float x, float y, float width, float height, Color startColor, Color endColor) {
            drawTriangle(x, y, startColor, x + width, y, startColor, x, y + height, endColor);
            drawTriangle(x + width, y, startColor, x + width, y + height, endColor, x, y + height, endColor);
        }

        public void drawVerticalGradient(float x, float y, float width, float height, Color startColor, Color endColor) {
            drawTriangle(x, y, startColor, x + width, y, endColor, x, y + height, startColor);
            drawTriangle(x + width, y, endColor, x + width, y + height, endColor, x, y + height, startColor);
        }

        public void clearStorageFrame() {
            UNIFORM_STORAGE.clear();
        }
    }

    public static class Render3D implements IRenderer {
        private final DynamicUniformStorage<UboData> UNIFORM_STORAGE = new DynamicUniformStorage<>("Aoba 3D UBO",
                new Std140SizeCalculator().putMat4f().putMat4f().get(), 16);
        private final UboData UBO_DATA = new UboData();

        private final BufferManager triangleBuffer;
        private final BufferManager lineBuffer;
        private boolean isBuilding = false;
        private int currentVertexIndex = 0;

        public Render3D() {
            this.triangleBuffer = new BufferManager(28);
            this.lineBuffer = new BufferManager(28);
        }

        @Override
        public void begin() {
            if (isBuilding) {
                throw new IllegalStateException("Renderer is already building");
            }
            isBuilding = true;
            triangleBuffer.clear();
            lineBuffer.clear();
            currentVertexIndex = 0;
        }

        @Override
        public void end() {
            if (!isBuilding) {
                throw new IllegalStateException("Renderer is not building");
            }
            isBuilding = false;
        }

        @Override
        public boolean isBuilding() {
            return isBuilding;
        }

        @Override
        public void reset() {
            triangleBuffer.clear();
            lineBuffer.clear();
            currentVertexIndex = 0;
        }

        @Override
        public void render() {
            try {
                if (!triangleBuffer.isEmpty()) {
                    renderBuffer(triangleBuffer, AobaRenderPipelines.TRIS);
                }
                if (!lineBuffer.isEmpty()) {
                    renderBuffer(lineBuffer, AobaRenderPipelines.LINES);
                }
            } finally {
                reset();
            }
        }

        private void renderBuffer(BufferManager buffer, RenderPipeline pipeline) {
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

            GpuTextureView colorAttachment = framebuffer.getColorAttachmentView();
            GpuBuffer vertexBuffer = buffer.createVertexBuffer(VertexFormats.POSITION_COLOR);
            GpuBuffer indexBuffer = buffer.createIndexBuffer(VertexFormats.POSITION_COLOR);

            UBO_DATA.proj = RenderManager.projection;
            UBO_DATA.modelView = RenderSystem.getModelViewMatrix();

            GpuBufferSlice matrixData = UNIFORM_STORAGE.write(UBO_DATA);

            RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Aoba 3D Renderer",
                    colorAttachment, OptionalInt.empty());

            pass.setPipeline(pipeline);
            pass.setUniform("Matrices", matrixData);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
            pass.drawIndexed(0, 0, buffer.getIndexCount(), 1);
            pass.close();
        }

        public void drawBox(Box box, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            Box offsetBox = box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            int startVertex = currentVertexIndex;

            triangleBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.minY, (float) offsetBox.minZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.minY, (float) offsetBox.minZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.maxY, (float) offsetBox.minZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.maxY, (float) offsetBox.minZ);
            triangleBuffer.addColor(color);

            triangleBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.minY, (float) offsetBox.maxZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.minY, (float) offsetBox.maxZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.maxY, (float) offsetBox.maxZ);
            triangleBuffer.addColor(color);
            triangleBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.maxY, (float) offsetBox.maxZ);
            triangleBuffer.addColor(color);

            triangleBuffer.addTriangle(startVertex, startVertex + 1, startVertex + 2);
            triangleBuffer.addTriangle(startVertex, startVertex + 2, startVertex + 3);

            triangleBuffer.addTriangle(startVertex + 4, startVertex + 6, startVertex + 5);
            triangleBuffer.addTriangle(startVertex + 4, startVertex + 7, startVertex + 6);

            triangleBuffer.addTriangle(startVertex, startVertex + 4, startVertex + 5);
            triangleBuffer.addTriangle(startVertex, startVertex + 5, startVertex + 1);

            triangleBuffer.addTriangle(startVertex + 2, startVertex + 6, startVertex + 7);
            triangleBuffer.addTriangle(startVertex + 2, startVertex + 7, startVertex + 3);

            triangleBuffer.addTriangle(startVertex, startVertex + 3, startVertex + 7);
            triangleBuffer.addTriangle(startVertex, startVertex + 7, startVertex + 4);

            triangleBuffer.addTriangle(startVertex + 1, startVertex + 5, startVertex + 6);
            triangleBuffer.addTriangle(startVertex + 1, startVertex + 6, startVertex + 2);

            currentVertexIndex += 8;
        }

        public void drawBoxOutline(Box box, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            Box offsetBox = box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            int startVertex = currentVertexIndex;

            lineBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.minY, (float) offsetBox.minZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.minY, (float) offsetBox.minZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.maxY, (float) offsetBox.minZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.maxY, (float) offsetBox.minZ);
            lineBuffer.addColor(color);

            lineBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.minY, (float) offsetBox.maxZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.minY, (float) offsetBox.maxZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.maxX, (float) offsetBox.maxY, (float) offsetBox.maxZ);
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) offsetBox.minX, (float) offsetBox.maxY, (float) offsetBox.maxZ);
            lineBuffer.addColor(color);

            lineBuffer.addLine(startVertex, startVertex + 1);
            lineBuffer.addLine(startVertex + 1, startVertex + 2);
            lineBuffer.addLine(startVertex + 2, startVertex + 3);
            lineBuffer.addLine(startVertex + 3, startVertex);

            lineBuffer.addLine(startVertex + 4, startVertex + 5);
            lineBuffer.addLine(startVertex + 5, startVertex + 6);
            lineBuffer.addLine(startVertex + 6, startVertex + 7);
            lineBuffer.addLine(startVertex + 7, startVertex + 4);

            lineBuffer.addLine(startVertex, startVertex + 4);
            lineBuffer.addLine(startVertex + 1, startVertex + 5);
            lineBuffer.addLine(startVertex + 2, startVertex + 6);
            lineBuffer.addLine(startVertex + 3, startVertex + 7);

            currentVertexIndex += 8;
        }

        public void drawLine(Vec3d start, Vec3d end, Color color) {
            drawLine(start.x, start.y, start.z, end.x, end.y, end.z, color);
        }

        public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            int startVertex = currentVertexIndex;

            lineBuffer.addVertex((float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z));
            lineBuffer.addColor(color);
            lineBuffer.addVertex((float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z));
            lineBuffer.addColor(color);

            lineBuffer.addLine(startVertex, startVertex + 1);

            currentVertexIndex += 2;
        }

        public void drawSphere(Vec3d center, float radius, Color color) {
            drawSphere(center.x, center.y, center.z, radius, color);
        }

        public void drawSphere(double x, double y, double z, float radius, Color color) {
            if (!isBuilding) {
                throw new IllegalStateException("Must call begin() before drawing");
            }

            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            int rings = 16;
            int sectors = 16;

            float centerX = (float) (x - cameraPos.x);
            float centerY = (float) (y - cameraPos.y);
            float centerZ = (float) (z - cameraPos.z);

            int startVertex = currentVertexIndex;

            for (int r = 0; r <= rings; r++) {
                float lat = (float) (Math.PI * r / rings - Math.PI / 2);
                float y1 = (float) Math.sin(lat) * radius;
                float ringRadius = (float) Math.cos(lat) * radius;

                for (int s = 0; s <= sectors; s++) {
                    float lng = (float) (2 * Math.PI * s / sectors);
                    float x1 = (float) Math.cos(lng) * ringRadius;
                    float z1 = (float) Math.sin(lng) * ringRadius;

                    triangleBuffer.addVertex(centerX + x1, centerY + y1, centerZ + z1);
                    triangleBuffer.addColor(color);
                    currentVertexIndex++;

                    if (r < rings && s < sectors) {
                        int current = startVertex + r * (sectors + 1) + s;
                        int next = current + sectors + 1;

                        triangleBuffer.addTriangle(current, next, current + 1);
                        triangleBuffer.addTriangle(current + 1, next, next + 1);
                    }
                }
            }
        }

        public void clearStorageFrame() {
            UNIFORM_STORAGE.clear();
        }

        /**
         * Gets the interpolated position of the entity given a tick delta.
         *
         * @param entity Entity to get position of
         * @param delta  Tick delta.
         * @return Vec3d representing the interpolated position of the entity.
         */
        public static Vec3d getEntityPositionInterpolated(Entity entity, float delta) {
            return new Vec3d(MathHelper.lerp(delta, entity.lastX, entity.getX()),
                    MathHelper.lerp(delta, entity.lastY, entity.getY()),
                    MathHelper.lerp(delta, entity.lastZ, entity.getZ()));
        }

        /**
         * Gets the difference between the interpolated position and
         *
         * @param entity Entity to get position of
         * @param delta  Tick delta.
         * @return Vec3d representing the interpolated position of the entity.
         */
        public static Vec3d getEntityPositionOffsetInterpolated(Entity entity, float delta) {
            Vec3d interpolated = getEntityPositionInterpolated(entity, delta);
            return entity.getEntityPos().subtract(interpolated);
        }
    }

    public static class RenderContext {
        private final DrawContext drawContext;
        private final Matrix3x2fStack matrixStack;
        private final Matrix4f projection;
        private final Matrix4f modelView;
        private final Camera camera;
        private final float tickDelta;
        private final int screenWidth;
        private final int screenHeight;

        public RenderContext(DrawContext drawContext, float tickDelta) {
            this.drawContext = drawContext;
            this.matrixStack = drawContext.getMatrices();
            this.projection = new Matrix4f();
            this.modelView = new Matrix4f();
            this.camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            this.tickDelta = tickDelta;
            this.screenWidth = drawContext.getScaledWindowWidth();
            this.screenHeight = drawContext.getScaledWindowHeight();
        }

        public DrawContext getDrawContext() { return drawContext; }
        public Matrix3x2fStack getMatrixStack() { return matrixStack; }
        public Matrix4f getProjection() { return projection; }
        public Matrix4f getModelView() { return modelView; }
        public Camera getCamera() { return camera; }
        public float getTickDelta() { return tickDelta; }
        public int getScreenWidth() { return screenWidth; }
        public int getScreenHeight() { return screenHeight; }
    }

    public static class RenderManager {
        private static final RenderManager INSTANCE = new RenderManager();
        public static Matrix4f projection = new Matrix4f();
        public static final Matrix4f view = new Matrix4f();
        public static Vec3d center;

        private final Render2D render2D;
        private final List<IRenderer> activeRenderers;

        private RenderManager() {
            this.render2D = new Render2D();
            this.activeRenderers = new ArrayList<>();
        }

        public static void updateRenderProperties(Matrix4f proj, Matrix4f view) {
            projection.set(proj);

            Matrix4f invProjection = new Matrix4f(projection).invert();
            Matrix4f invView = new Matrix4f(view).invert();

            Vector4f center4 = new Vector4f(0, 0, 0, 1).mul(invProjection).mul(invView);
            center4.div(center4.w);

            Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            center = new Vec3d(camera.x + center4.x, camera.y + center4.y, camera.z + center4.z);
        }

        public static RenderManager getInstance() {
            return INSTANCE;
        }

        public Render2D get2D() {
            return render2D;
        }

        public void beginFrame(IRenderer renderer) {
            renderer.clearStorageFrame();
            activeRenderers.clear();
            registerRenderer(renderer);
        }

        public void endFrame() {
            for (IRenderer renderer : activeRenderers) {
                if (renderer.isBuilding()) {
                    renderer.end();
                }
                renderer.render();
            }
            activeRenderers.clear();
        }

        public void registerRenderer(IRenderer renderer) {
            if (!activeRenderers.contains(renderer)) {
                activeRenderers.add(renderer);
            }
        }
    }

    public static class UboData implements DynamicUniformStorage.Uploadable {
        public Matrix4f proj;
        public Matrix4f modelView;

        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putMat4f(proj).putMat4f(modelView);
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
