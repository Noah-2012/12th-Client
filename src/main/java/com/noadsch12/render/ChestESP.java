package com.noadsch12.render;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChestESP {

    // Persistent storage: Blocks are only removed if broken/unloaded
    private static final Map<BlockPos, Integer> cachedBlocks = new ConcurrentHashMap<>();
    private static ChunkPos lastChunkPos = null;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        // Adds blocks to cache when they load into the world
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity blockEntity, ClientWorld world) -> {
            updateSingleBlock(blockEntity.getPos(), blockEntity.getCachedState());
        });

        // Removes blocks ONLY when they are actually removed/unloaded
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity blockEntity, ClientWorld world) -> {
            cachedBlocks.remove(blockEntity.getPos());
        });

        initialized = true;
    }

    public static void render(DrawContext context) {
        if (!ClientSettingsScreen.ChestESPEnabled) return;
        init();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        // Detect if player moved to a new chunk
        ChunkPos currentChunk = new ChunkPos(client.player.getBlockPos());
        if (lastChunkPos == null || !currentChunk.equals(lastChunkPos)) {
            lastChunkPos = currentChunk;
            // Only scan the current chunk you just entered to fill in gaps
            scanChunk(client, currentChunk);
        }

        renderAll(context, client);
    }

    private static void renderAll(DrawContext context, MinecraftClient client) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float fov = (float) client.options.getFov().getValue();
        Matrix4f projMat = client.gameRenderer.getBasicProjectionMatrix(fov);
        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Matrix4f viewMat = new Matrix4f().rotation(camera.getRotation().conjugate());

        cachedBlocks.forEach((pos, color) -> {
            // Optional: You could add a distance check here if you want to limit
            // the tracers to a certain range (e.g., 200 blocks) to prevent clutter.
            renderTarget(context, client, pos.toCenterPos(), camPos, projMat, viewMat, sw, sh, color);
        });
    }

    private static void scanChunk(MinecraftClient client, ChunkPos chunkPos) {
        if (client.world == null) return;

        BlockPos start = chunkPos.getStartPos();

        // Use getBottomY and getHeight to define the loop range without using Heightmaps
        int minY = client.world.getBottomY();
        int maxY = minY + client.world.getHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    // We use the temporary BlockPos.Mutable to reduce object creation lag
                    BlockPos look = start.add(x, y, z);
                    updateSingleBlock(look, client.world.getBlockState(look));
                }
            }
        }
    }

    private static void updateSingleBlock(BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        int color = -1;

        if (b == Blocks.CHEST) color = pack(255, 160, 0, 255);
        else if (b == Blocks.TRAPPED_CHEST) color = pack(255, 0, 0, 255);
        else if (b == Blocks.BARREL) color = pack(255, 160, 0, 255);
        else if (b == Blocks.ENDER_CHEST) color = pack(120, 0, 255, 255);
        else if (b instanceof ShulkerBoxBlock) color = pack(255, 160, 0, 255);
        else if (b instanceof AbstractFurnaceBlock || b instanceof DispenserBlock ||
                b instanceof HopperBlock || b == Blocks.DROPPER) {
            color = pack(140, 140, 140, 255);
        }

        if (color != -1) {
            cachedBlocks.put(pos, color);
        }
    }

    private static void renderTarget(DrawContext context, MinecraftClient client, Vec3d target,
                                     Vec3d camPos, Matrix4f proj, Matrix4f view, int sw, int sh, int color) {
        float dx = (float) (target.x - camPos.x);
        float dy = (float) (target.y - camPos.y);
        float dz = (float) (target.z - camPos.z);

        Vector4f pos = new Vector4f(dx, dy, dz, 1.0f);
        pos.mul(view).mul(proj);

        if (pos.w <= 0) return;

        float screenX = ((pos.x / pos.w) + 1.0f) * sw / 2.0f;
        float screenY = (1.0f - (pos.y / pos.w)) * sh / 2.0f;

        drawTracer(context, sw / 2f, sh / 2f, screenX, screenY, color);

        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(screenX, screenY);

        double dist = target.distanceTo(camPos);
        int s = (int) MathHelper.clamp(40.0 / (dist * 0.4), 3, 20);

        context.fill(-s, -s, s, -s + 1, color);
        context.fill(-s, s - 1, s, s, color);
        context.fill(-s, -s + 1, -s + 1, s - 1, color);
        context.fill(s - 1, -s + 1, s, s - 1, color);

        stack.popMatrix();
    }

    private static void drawTracer(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1, dy = y2 - y1;
        var stack = context.getMatrices();
        stack.pushMatrix();
        stack.translate(x1, y1);
        stack.rotate((float) Math.atan2(dy, dx));
        context.fill(0, 0, (int) Math.sqrt(dx * dx + dy * dy), 1, color);
        stack.popMatrix();
    }

    private static int pack(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}