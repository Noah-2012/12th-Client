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

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noadsch12.TwelfthClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/**
 * General-purpose 3-D rendering utilities for the 12th Client.
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li>Provide the two standard {@link RenderLayer}s (depth-tested and
 *       always-on-top / no-depth) that every visual feature needs.</li>
 *   <li>Centralise all low-level vertex emission so feature modules never
 *       touch {@link VertexConsumer} directly for common shapes.</li>
 *   <li>Keep every helper stateless (no shared mutable state) so callers
 *       can mix and match freely inside a single render pass.</li>
 * </ul>
 *
 * <h2>Usage sketch</h2>
 * <pre>{@code
 *  VertexConsumer buf = consumers.getBuffer(RenderUtils.LINES_NO_DEPTH);
 *  Vec3d cam = context.gameRenderer().getCamera().getPos();
 *
 *  MatrixStack matrices = context.matrices();
 *
 *  // Wireframe box around a block, no depth test, orange
 *  RenderUtils.drawBlockBox(matrices, buf, cam,
 *          someBlockPos, RenderUtils.COLOR_CHEST);
 *
 *  // Filled (quad) highlight on the top face of the same block
 *  VertexConsumer fill = consumers.getBuffer(RenderUtils.QUADS_NO_DEPTH);
 *  RenderUtils.drawBlockTopFace(matrices, fill, cam,
 *          someBlockPos, 1f, 0.63f, 0f, 0.25f);
 * }</pre>
 */
public final class RenderUtils {

    // =========================================================================
    // RenderLayers
    // =========================================================================

    /**
     * Line render layer <b>with</b> the normal depth test (LEQUAL).
     * Lines are occluded by geometry in front of them.
     */
    public static final RenderLayer LINES_DEPTH = RenderLayer.of(
            "twelfth_lines_depth",
            1536,
            false,
            true,
            RenderPipeline.builder()
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog",               UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals",           UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection",        UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withLocation(TwelfthClient.identifier("lines_depth"))
                    .build(),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    /**
     * Line render layer with <b>no</b> depth test.
     * Lines are always drawn on top of all world geometry — ideal for ESP.
     */
    public static final RenderLayer LINES_NO_DEPTH = RenderLayer.of(
            "twelfth_lines_no_depth",
            1536,
            false,
            true,
            RenderPipeline.builder()
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog",               UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals",           UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection",        UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withLocation(TwelfthClient.identifier("lines_depth"))
                    .build(),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    /**
     * Quad render layer with <b>no</b> depth test and translucent blending.
     * Use this for filled / semi-transparent face highlights drawn on top of
     * all world geometry.
     */
    public static final RenderLayer QUADS_NO_DEPTH = RenderLayer.of(
            "twelfth_quads_no_depth",
            1536,
            false,
            true,
            RenderPipeline.builder()
                    .withVertexShader("core/rendertype_position_color")
                    .withFragmentShader("core/rendertype_position_color")
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog",               UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals",           UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection",        UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withLocation(TwelfthClient.identifier("quads_no_depth"))
                    .build(),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    /**
     * Quad render layer <b>with</b> the normal depth test (LEQUAL) and
     * translucent blending.  Use this for filled face highlights that respect
     * world geometry occlusion.
     */
    public static final RenderLayer QUADS_DEPTH = RenderLayer.of(
            "twelfth_quads_depth",
            1536,
            false,
            true,
            RenderPipeline.builder()
                    .withVertexShader("core/rendertype_position_color")
                    .withFragmentShader("core/rendertype_position_color")
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog",               UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals",           UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection",        UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withLocation(TwelfthClient.identifier("quads_depth"))
                    .build(),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    // =========================================================================
    // Built-in color palette  (r, g, b — all in 0-1 range)
    // =========================================================================

    /** Chest / barrel / shulker box — warm orange. */
    public static final float[] COLOR_CHEST        = {1.00f, 0.63f, 0.00f};
    /** Trapped chest — red. */
    public static final float[] COLOR_TRAPPED      = {1.00f, 0.00f, 0.00f};
    /** Ender chest — vivid purple. */
    public static final float[] COLOR_ENDER_CHEST  = {0.47f, 0.00f, 1.00f};
    /** Furnace / hopper / dispenser — mid-grey. */
    public static final float[] COLOR_MACHINE      = {0.55f, 0.55f, 0.55f};
    /** Generic hostile / enemy — red. */
    public static final float[] COLOR_HOSTILE      = {1.00f, 0.00f, 0.00f};
    /** Friendly / neutral — green. */
    public static final float[] COLOR_FRIENDLY     = {0.00f, 1.00f, 0.00f};
    /** Self — white. */
    public static final float[] COLOR_SELF         = {1.00f, 1.00f, 1.00f};

    // =========================================================================
    // Private constructor — utility class, do not instantiate.
    // =========================================================================
    private RenderUtils() {}

    // =========================================================================
    // RenderSystem state helpers
    // =========================================================================

    /**
     * Enables anti-aliased line rendering and sets the global line width.
     * Call once before a batch of line draws; pair with {@link #endLines()}.
     *
     * @param lineWidth desired width in pixels (vanilla uses 1.0 or 2.0)
     */
    public static void beginLines(float lineWidth, boolean withSmooth) {
        if (withSmooth) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        }
        RenderSystem.lineWidth(lineWidth);
    }

    /**
     * Restores default line rendering state after a batch of line draws.
     */
    public static void endLines() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.0f);
    }

    // =========================================================================
    // Camera helpers
    // =========================================================================

    /**
     * Returns the interpolated eye position of the local player at the given
     * partial tick, expressed in absolute world space.
     *
     * @param partialTick render partial tick (usually {@code context.tickCounter().getTickDelta(true)})
     * @return eye position, or {@link Vec3d#ZERO} when the player is absent
     */
    public static Vec3d getPlayerEyePos(float partialTick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return Vec3d.ZERO;
        return mc.player.getCameraPosVec(partialTick);
    }

    /**
     * Derives the tracer "origin" from the camera: a point slightly in front
     * of the near-clip plane along the camera's look direction.
     * This prevents degenerate zero-length lines in first-person view.
     *
     * @param camera the current {@link Camera}
     * @param offset distance in blocks to push forward (0.5 is usually fine)
     * @return world-space origin for tracer lines
     */
    public static Vec3d getTracerOrigin(Camera camera, float offset) {
        Vec3d camPos = camera.getPos();
        Vector3f forward = new Vector3f(0f, 0f, -1f).rotate(camera.getRotation());
        return camPos.add(forward.x * offset, forward.y * offset, forward.z * offset);
    }

    /** Convenience overload with the standard 0.5-block offset. */
    public static Vec3d getTracerOrigin(Camera camera) {
        return getTracerOrigin(camera, 0.5f);
    }

    // =========================================================================
    // Matrix helpers
    // =========================================================================

    /**
     * Translates {@code matrices} so that the origin sits at
     * {@code worldPos} expressed camera-relative.
     * The caller is responsible for {@link MatrixStack#push()} /
     * {@link MatrixStack#pop()}.
     *
     * @param matrices  the active matrix stack
     * @param worldPos  absolute world-space position
     * @param camera    camera position (from {@link Camera#getPos()})
     */
    public static void translateCameraRelative(MatrixStack matrices, Vec3d worldPos, Vec3d camera) {
        matrices.translate(
                worldPos.x - camera.x,
                worldPos.y - camera.y,
                worldPos.z - camera.z
        );
    }

    /**
     * Overload accepting a {@link BlockPos} — translates to the block origin
     * (its minimum corner) in camera-relative space.
     */
    public static void translateCameraRelative(MatrixStack matrices, BlockPos pos, Vec3d camera) {
        matrices.translate(
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z
        );
    }

    // =========================================================================
    // Color helpers
    // =========================================================================

    /**
     * Packs r/g/b floats (0-1) and an alpha float (0-1) into an ARGB int,
     * useful when interfacing with vanilla rendering code that expects packed
     * colors.
     */
    public static int packARGB(float r, float g, float b, float a) {
        return ((int)(a * 255) << 24)
                | ((int)(r * 255) << 16)
                | ((int)(g * 255) << 8)
                |  (int)(b * 255);
    }

    /**
     * Unpacks an ARGB int into a float[4] array {@code {r, g, b, a}} with
     * components in the 0-1 range.
     */
    public static float[] unpackARGB(int argb) {
        return new float[]{
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >>  8) & 0xFF) / 255f,
                (argb        & 0xFF) / 255f,
                ((argb >> 24) & 0xFF) / 255f
        };
    }

    /**
     * Linearly interpolates between two RGB colour arrays by factor {@code t}.
     *
     * @param a  start colour (float[3])
     * @param b  end colour (float[3])
     * @param t  blend factor 0-1
     * @return   new float[3]
     */
    public static float[] lerpColor(float[] a, float[] b, float t) {
        return new float[]{
                a[0] + (b[0] - a[0]) * t,
                a[1] + (b[1] - a[1]) * t,
                a[2] + (b[2] - a[2]) * t
        };
    }

    // =========================================================================
    // Primitive emission — lines
    // =========================================================================

    /**
     * Emits a single line segment into {@code buf}.
     * The direction normal is computed automatically from the two endpoints.
     *
     * <p>The caller must have obtained {@code buf} from a line-mode
     * {@link RenderLayer} (e.g. {@link #LINES_NO_DEPTH}).
     *
     * @param buf    target vertex consumer
     * @param model  current model matrix (from {@link MatrixStack.Entry#getPositionMatrix()})
     * @param x0     start X (camera-relative)
     * @param y0     start Y
     * @param z0     start Z
     * @param x1     end X
     * @param y1     end Y
     * @param z1     end Z
     * @param r      red   (0-1)
     * @param g      green (0-1)
     * @param b      blue  (0-1)
     * @param a      alpha (0-1)
     */
    public static void emitLine(VertexConsumer buf, Matrix4f model,
                                float x0, float y0, float z0,
                                float x1, float y1, float z1,
                                float r, float g, float b, float a) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0f) return;   // degenerate — skip silently
        float nx = dx / len, ny = dy / len, nz = dz / len;

        buf.vertex(model, x0, y0, z0).color(r, g, b, a).normal(nx,  ny,  nz);
        buf.vertex(model, x1, y1, z1).color(r, g, b, a).normal(nx,  ny,  nz);
    }

    /** Convenience overload with full opacity. */
    public static void emitLine(VertexConsumer buf, Matrix4f model,
                                float x0, float y0, float z0,
                                float x1, float y1, float z1,
                                float r, float g, float b) {
        emitLine(buf, model, x0, y0, z0, x1, y1, z1, r, g, b, 1f);
    }

    // =========================================================================
    // Primitive emission — quads
    // =========================================================================

    /**
     * Emits a single axis-aligned quad (four vertices, counter-clockwise from
     * bottom-left) into {@code buf}.
     *
     * <p>The caller must have obtained {@code buf} from a quad-mode
     * {@link RenderLayer} (e.g. {@link #QUADS_NO_DEPTH}).
     *
     * @param buf   target vertex consumer
     * @param model current model matrix
     * @param x0    min X
     * @param y0    min Y
     * @param z0    min Z
     * @param x1    max X
     * @param y1    max Y
     * @param z1    max Z (only two of these three "max" axes are used per face)
     * @param r red (0-1)
     * @param g green (0-1)
     * @param b blue (0-1)
     * @param a alpha (0-1)
     */
    public static void emitQuadXZ(VertexConsumer buf, Matrix4f model,
                                  float x0, float y, float z0,
                                  float x1,          float z1,
                                  float r, float g, float b, float a) {
        buf.vertex(model, x0, y, z0).color(r, g, b, a);
        buf.vertex(model, x1, y, z0).color(r, g, b, a);
        buf.vertex(model, x1, y, z1).color(r, g, b, a);
        buf.vertex(model, x0, y, z1).color(r, g, b, a);
    }

    /** Emits a filled quad on the XY plane at constant Z. */
    public static void emitQuadXY(VertexConsumer buf, Matrix4f model,
                                  float x0, float y0, float z,
                                  float x1, float y1,
                                  float r, float g, float b, float a) {
        buf.vertex(model, x0, y0, z).color(r, g, b, a);
        buf.vertex(model, x1, y0, z).color(r, g, b, a);
        buf.vertex(model, x1, y1, z).color(r, g, b, a);
        buf.vertex(model, x0, y1, z).color(r, g, b, a);
    }

    /** Emits a filled quad on the YZ plane at constant X. */
    public static void emitQuadYZ(VertexConsumer buf, Matrix4f model,
                                  float x,
                                  float y0, float z0,
                                  float y1, float z1,
                                  float r, float g, float b, float a) {
        buf.vertex(model, x, y0, z0).color(r, g, b, a);
        buf.vertex(model, x, y0, z1).color(r, g, b, a);
        buf.vertex(model, x, y1, z1).color(r, g, b, a);
        buf.vertex(model, x, y1, z0).color(r, g, b, a);
    }

    // =========================================================================
    // Compound shapes — wireframe
    // =========================================================================

    /**
     * Draws all 12 edges of an axis-aligned wireframe box.
     *
     * <p>The coordinates are model-space (i.e. already translated to be
     * camera-relative).  For a 1×1×1 block box pass
     * {@code 0,0,0} → {@code 1,1,1}.
     *
     * @param buf   line vertex consumer (e.g. from {@link #LINES_NO_DEPTH})
     * @param model current model matrix
     * @param x0 min X
     * @param y0 min Y
     * @param z0 min Z
     * @param x1 max X
     * @param y1 max Y
     * @param z1 max Z
     * @param r red   (0-1)
     * @param g green (0-1)
     * @param b blue  (0-1)
     * @param a alpha (0-1)
     */
    public static void drawWireframeBox(VertexConsumer buf, Matrix4f model,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1,
                                        float r, float g, float b, float a,
                                        boolean baritoneStyle) {
        // Bottom face
        emitLine(buf, model, x0, y0, z0, x1, y0, z0, r, g, b, a);
        emitLine(buf, model, x1, y0, z0, x1, y0, z1, r, g, b, a);
        emitLine(buf, model, x1, y0, z1, x0, y0, z1, r, g, b, a);
        emitLine(buf, model, x0, y0, z1, x0, y0, z0, r, g, b, a);
        // Top face
        emitLine(buf, model, x0, y1, z0, x1, y1, z0, r, g, b, a);
        emitLine(buf, model, x1, y1, z0, x1, y1, z1, r, g, b, a);
        emitLine(buf, model, x1, y1, z1, x0, y1, z1, r, g, b, a);
        emitLine(buf, model, x0, y1, z1, x0, y1, z0, r, g, b, a);
        // Vertical edges
        emitLine(buf, model, x0, y0, z0, x0, y1, z0, r, g, b, a);
        emitLine(buf, model, x1, y0, z0, x1, y1, z0, r, g, b, a);
        emitLine(buf, model, x1, y0, z1, x1, y1, z1, r, g, b, a);
        emitLine(buf, model, x0, y0, z1, x0, y1, z1, r, g, b, a);

        if (baritoneStyle) {
            // --- time-based cosine wave (same idea as Baritone) ---
            double time = (System.nanoTime() / 100000L) % 20000L;
            float wave = (float) Math.cos((time / 20000.0) * Math.PI * 2);

            // scale the movement (tweak this!)
            float amplitude = (y1 - y0) * 0.5f;

            float mid = (y0 + y1) * 0.5f;

            float yTop = mid + wave * amplitude;
            float yBottom = mid - wave * amplitude;

            // --- draw the two animated horizontal outlines ---
            // Top moving line
            emitLine(buf, model, x0, yTop, z0, x1, yTop, z0, r, g, b, a);
            emitLine(buf, model, x1, yTop, z0, x1, yTop, z1, r, g, b, a);
            emitLine(buf, model, x1, yTop, z1, x0, yTop, z1, r, g, b, a);
            emitLine(buf, model, x0, yTop, z1, x0, yTop, z0, r, g, b, a);

            // Bottom moving line (mirrored)
            emitLine(buf, model, x0, yBottom, z0, x1, yBottom, z0, r, g, b, a);
            emitLine(buf, model, x1, yBottom, z0, x1, yBottom, z1, r, g, b, a);
            emitLine(buf, model, x1, yBottom, z1, x0, yBottom, z1, r, g, b, a);
            emitLine(buf, model, x0, yBottom, z1, x0, yBottom, z0, r, g, b, a);
        }
    }

    /** Convenience overload with full opacity. */
    public static void drawWireframeBox(VertexConsumer buf, Matrix4f model,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1,
                                        float r, float g, float b,
                                        boolean baritoneStyle) {
        drawWireframeBox(buf, model, x0, y0, z0, x1, y1, z1, r, g, b, 1f, baritoneStyle);
    }

    /**
     * Draws a wireframe box from a vanilla {@link Box} in camera-relative space.
     *
     * @param matrices the active matrix stack (push/pop is handled internally)
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param box      the box in absolute world coordinates
     * @param r        red   (0-1)
     * @param g        green (0-1)
     * @param b        blue  (0-1)
     * @param a        alpha (0-1)
     */
    public static void drawWireframeBox(MatrixStack matrices, VertexConsumer buf,
                                        Vec3d camera, Box box,
                                        float r, float g, float b, float a,
                                        boolean baritoneStyle) {
        matrices.push();
        matrices.translate(
                box.minX - camera.x,
                box.minY - camera.y,
                box.minZ - camera.z
        );
        Matrix4f model = matrices.peek().getPositionMatrix();
        float w = (float)(box.maxX - box.minX);
        float h = (float)(box.maxY - box.minY);
        float d = (float)(box.maxZ - box.minZ);
        drawWireframeBox(buf, model, 0f, 0f, 0f, w, h, d, r, g, b, a, baritoneStyle);
        matrices.pop();
    }

    // =========================================================================
    // Compound shapes — block helpers
    // =========================================================================

    /**
     * Draws a 1×1×1 wireframe box around a single {@link BlockPos}, with the
     * standard small inset (expand –0.005) to avoid Z-fighting with block faces.
     *
     * @param matrices the active matrix stack
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param pos      target block position
     * @param rgb      float[3] colour (from the palette constants, e.g. {@link #COLOR_CHEST})
     */
    public static void drawBlockBox(MatrixStack matrices, VertexConsumer buf,
                                    Vec3d camera, BlockPos pos, float[] rgb,
                                    boolean baritoneStyle) {
        drawBlockBox(matrices, buf, camera, pos, rgb[0], rgb[1], rgb[2], 1f, baritoneStyle);
    }

    /**
     * Full-parameter variant of {@link #drawBlockBox(MatrixStack, VertexConsumer, Vec3d, BlockPos, float[], boolean)}.
     */
    public static void drawBlockBox(MatrixStack matrices, VertexConsumer buf,
                                    Vec3d camera, BlockPos pos,
                                    float r, float g, float b, float a,
                                    boolean baritoneStyle) {
        final float E = 0.005f; // tiny expand to avoid z-fighting
        matrices.push();
        translateCameraRelative(matrices, pos, camera);
        Matrix4f model = matrices.peek().getPositionMatrix();
        drawWireframeBox(buf, model, -E, -E, -E, 1f + E, 1f + E, 1f + E, r, g, b, a, baritoneStyle);
        matrices.pop();
    }

    /**
     * Draws a filled (quad) highlight on the top face of a block.
     * Typically rendered with {@link #QUADS_NO_DEPTH} for a glow-style overlay.
     *
     * @param matrices active matrix stack
     * @param buf      quad vertex consumer
     * @param camera   camera world position
     * @param pos      target block position
     * @param r        red
     * @param g        green
     * @param b        blue
     * @param a        alpha
     */
    public static void drawBlockTopFace(MatrixStack matrices, VertexConsumer buf,
                                        Vec3d camera, BlockPos pos,
                                        float r, float g, float b, float a) {
        matrices.push();
        translateCameraRelative(matrices, pos, camera);
        Matrix4f model = matrices.peek().getPositionMatrix();
        emitQuadXZ(buf, model, 0f, 1f, 0f, 1f, 1f, r, g, b, a);
        matrices.pop();
    }

    /**
     * Draws all six filled faces of a block cube (useful for a solid highlight
     * effect).  Each face is one quad emitted into {@code buf}.
     */
    public static void drawBlockFilledBox(MatrixStack matrices, VertexConsumer buf,
                                          Vec3d camera, BlockPos pos,
                                          float r, float g, float b, float a) {
        matrices.push();
        translateCameraRelative(matrices, pos, camera);
        Matrix4f m = matrices.peek().getPositionMatrix();

        // Bottom (y=0, normal down)
        emitQuadXZ(buf, m, 0f, 0f, 0f, 1f, 1f, r, g, b, a);
        // Top (y=1, normal up)
        emitQuadXZ(buf, m, 0f, 1f, 0f, 1f, 1f, r, g, b, a);
        // North (z=0)
        emitQuadXY(buf, m, 0f, 0f, 0f, 1f, 1f, r, g, b, a);
        // South (z=1)
        emitQuadXY(buf, m, 0f, 0f, 1f, 1f, 1f, r, g, b, a);
        // West (x=0)
        emitQuadYZ(buf, m, 0f, 0f, 0f, 1f, 1f, r, g, b, a);
        // East (x=1)
        emitQuadYZ(buf, m, 1f, 0f, 0f, 1f, 1f, r, g, b, a);

        matrices.pop();
    }

    // =========================================================================
    // Compound shapes — block selection box (vanilla-style)
    // =========================================================================

    /**
     * Draws a vanilla-style block selection outline.
     * The box is slightly expanded on all sides (0.002f) to sit just above the
     * block surface, and uses the same 12-edge wireframe as vanilla's own
     * selection box rendering.
     *
     * @param matrices active matrix stack
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param pos      target block position
     * @param r        red
     * @param g        green
     * @param b        blue
     * @param a        alpha
     */
    public static void drawBlockSelection(MatrixStack matrices, VertexConsumer buf,
                                          Vec3d camera, BlockPos pos,
                                          float r, float g, float b, float a,
                                          boolean baritoneStyle) {
        final float E = 0.002f;
        matrices.push();
        translateCameraRelative(matrices, pos, camera);
        Matrix4f model = matrices.peek().getPositionMatrix();
        drawWireframeBox(buf, model, -E, -E, -E, 1f + E, 1f + E, 1f + E, r, g, b, a, baritoneStyle);
        matrices.pop();
    }

    /**
     * Overload accepting a {@link Box} for non-unit shapes (doors, slabs, etc.).
     */
    public static void drawBlockSelection(MatrixStack matrices, VertexConsumer buf,
                                          Vec3d camera, Box shape,
                                          float r, float g, float b, float a,
                                          boolean baritoneStyle) {
        final float E = 0.002f;
        drawWireframeBox(matrices, buf, camera,
                shape.expand(E), r, g, b, a, baritoneStyle);
    }

    // =========================================================================
    // Compound shapes — entity / player wireframe box
    // =========================================================================

    /**
     * Draws a wireframe box sized for a player (0.6 wide × 1.8 tall) centred
     * on the given eye position.
     *
     * @param matrices active matrix stack
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param eyePos   absolute world-space eye position of the target player
     * @param r        red
     * @param g        green
     * @param b        blue
     * @param a        alpha
     */
    public static void drawPlayerBox(MatrixStack matrices, VertexConsumer buf,
                                     Vec3d camera, Vec3d eyePos,
                                     float r, float g, float b, float a,
                                     boolean baritoneStyle) {
        final float hw      = 0.30f;   // half width  → full = 0.6
        final float eyeY    = 0.15f;   // offset of eye from "box centre Y"
        final float headTop = eyeY + 0.27f;
        final float feetBot = eyeY - 1.53f;
        drawEntityBox(matrices, buf, camera, eyePos,
                -hw, feetBot, -hw, hw, headTop, hw,
                r, g, b, a, baritoneStyle);
    }

    /** Convenience overload using {@link #COLOR_HOSTILE} and full opacity. */
    public static void drawPlayerBox(MatrixStack matrices, VertexConsumer buf,
                                     Vec3d camera, Vec3d eyePos,
                                     boolean baritoneStyle) {
        drawPlayerBox(matrices, buf, camera, eyePos,
                COLOR_HOSTILE[0], COLOR_HOSTILE[1], COLOR_HOSTILE[2], 1f, baritoneStyle);
    }

    /**
     * Generic entity wireframe box, centred on {@code pivotPos}.
     * Use this for mobs — supply the correct half-dimensions from the entity's
     * bounding box.
     *
     * @param matrices  active matrix stack
     * @param buf       line vertex consumer
     * @param camera    camera world position
     * @param pivotPos  world-space pivot (typically the entity's eye position or
     *                  the centre of its bounding box)
     * @param minX      min X relative to pivot
     * @param minY      min Y relative to pivot
     * @param minZ      min Z relative to pivot
     * @param maxX      max X relative to pivot
     * @param maxY      max Y relative to pivot
     * @param maxZ      max Z relative to pivot
     * @param r         red
     * @param g         green
     * @param b         blue
     * @param a         alpha
     */
    public static void drawEntityBox(MatrixStack matrices, VertexConsumer buf,
                                     Vec3d camera, Vec3d pivotPos,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     float r, float g, float b, float a,
                                     boolean baritoneStyle) {
        matrices.push();
        translateCameraRelative(matrices, pivotPos, camera);
        Matrix4f model = matrices.peek().getPositionMatrix();
        drawWireframeBox(buf, model, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, baritoneStyle);
        matrices.pop();
    }

    /**
     * Draws a wireframe box around an entity using its live bounding box.
     *
     * @param matrices active matrix stack
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param entity   any living entity
     * @param partialTick render partial tick
     * @param r        red
     * @param g        green
     * @param b        blue
     * @param a        alpha
     */
    public static void drawEntityBox(MatrixStack matrices, VertexConsumer buf,
                                     Vec3d camera,
                                     Entity entity,
                                     float partialTick,
                                     float r, float g, float b, float a,
                                     boolean baritoneStyle) {
        // Interpolate the entity's position to avoid jitter
        double lx = MathHelper.lerp(partialTick,
                entity.lastRenderX, entity.getX());
        double ly = MathHelper.lerp(partialTick,
                entity.lastRenderY, entity.getY());
        double lz = MathHelper.lerp(partialTick,
                entity.lastRenderZ, entity.getZ());

        Box bb = entity.getBoundingBox().offset(lx - entity.getX(),
                ly - entity.getY(),
                lz - entity.getZ());
        drawWireframeBox(matrices, buf, camera, bb, r, g, b, a, baritoneStyle);
    }

    // =========================================================================
    // Miscellaneous geometry
    // =========================================================================

    /**
     * Draws a 2-D cross-hair "+" in world space — handy for debugging a world
     * position or highlighting a single point.
     *
     * @param matrices active matrix stack
     * @param buf      line vertex consumer
     * @param camera   camera world position
     * @param center   world-space position of the cross
     * @param size     arm length in blocks
     * @param r        red
     * @param g        green
     * @param b        blue
     * @param a        alpha
     */
    public static void drawCross(MatrixStack matrices, VertexConsumer buf,
                                 Vec3d camera, Vec3d center,
                                 float size,
                                 float r, float g, float b, float a) {
        matrices.push();
        translateCameraRelative(matrices, center, camera);
        Matrix4f m = matrices.peek().getPositionMatrix();
        emitLine(buf, m, -size, 0f, 0f, size, 0f, 0f, r, g, b, a);
        emitLine(buf, m, 0f, -size, 0f, 0f, size, 0f, r, g, b, a);
        emitLine(buf, m, 0f, 0f, -size, 0f, 0f, size, r, g, b, a);
        matrices.pop();
    }

    /**
     * Draws a horizontal circle (in the XZ plane) in world space.
     * Useful for range indicators or AoE visualisation.
     *
     * @param matrices  active matrix stack
     * @param buf       line vertex consumer
     * @param camera    camera world position
     * @param center    world-space centre of the circle
     * @param radius    radius in blocks
     * @param segments  number of line segments (32 looks smooth)
     * @param r         red
     * @param g         green
     * @param b         blue
     * @param a         alpha
     */
    public static void drawCircleXZ(MatrixStack matrices, VertexConsumer buf,
                                    Vec3d camera, Vec3d center,
                                    float radius, int segments,
                                    float r, float g, float b, float a) {
        matrices.push();
        translateCameraRelative(matrices, center, camera);
        Matrix4f m = matrices.peek().getPositionMatrix();

        float step = (float)(2.0 * Math.PI / segments);
        for (int i = 0; i < segments; i++) {
            float a0 = i * step;
            float a1 = (i + 1) * step;
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            emitLine(buf, m, x0, 0f, z0, x1, 0f, z1, r, g, b, a);
        }
        matrices.pop();
    }

    /**
     * Draws a vertical cylinder outline (two horizontal circles connected by
     * vertical lines at regular intervals) in world space.
     *
     * @param matrices  active matrix stack
     * @param buf       line vertex consumer
     * @param camera    camera world position
     * @param center    world-space centre of the bottom circle
     * @param radius    radius in blocks
     * @param height    height in blocks
     * @param segments  smoothness of the circles (32 recommended)
     * @param r         red
     * @param g         green
     * @param b         blue
     * @param a         alpha
     */
    public static void drawCylinder(MatrixStack matrices, VertexConsumer buf,
                                    Vec3d camera, Vec3d center,
                                    float radius, float height, int segments,
                                    float r, float g, float b, float a) {
        matrices.push();
        translateCameraRelative(matrices, center, camera);
        Matrix4f m = matrices.peek().getPositionMatrix();

        float step = (float)(2.0 * Math.PI / segments);
        for (int i = 0; i < segments; i++) {
            float a0 = i * step;
            float a1 = (i + 1) * step;
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            // Bottom ring
            emitLine(buf, m, x0, 0f,     z0, x1, 0f,     z1, r, g, b, a);
            // Top ring
            emitLine(buf, m, x0, height, z0, x1, height, z1, r, g, b, a);
            // Vertical struts (every 8th segment to avoid clutter)
            if (i % (Math.max(1, segments / 8)) == 0) {
                emitLine(buf, m, x0, 0f, z0, x0, height, z0, r, g, b, a);
            }
        }
        matrices.pop();
    }
}