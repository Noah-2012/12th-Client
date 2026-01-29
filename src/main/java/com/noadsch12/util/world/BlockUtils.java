package com.noadsch12.util.world;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;

public enum BlockUtils
{
    ;

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static BlockState getState(BlockPos pos)
    {
        return MC.world.getBlockState(pos);
    }

    public static Block getBlock(BlockPos pos)
    {
        return getState(pos).getBlock();
    }

    public static String getName(BlockPos pos)
    {
        return getName(getBlock(pos));
    }

    public static String getName(Block block)
    {
        return Registries.BLOCK.getId(block).toString();
    }

    public static Block getBlockFromName(String name)
    {
        try
        {
            return Registries.BLOCK.get(Identifier.tryParse(name));
        }
        catch (InvalidIdentifierException e)
        {
            return Blocks.AIR;
        }
    }

    public static Block getBlockFromNameOrID(String nameOrId)
    {
        // Legacy numeric IDs nicht mehr unterstützt
        if (isInteger(nameOrId))
            return null;

        try
        {
            Identifier id = Identifier.tryParse(nameOrId);
            if(!Registries.BLOCK.containsId(id))
                return null;

            return Registries.BLOCK.get(id);
        }
        catch (InvalidIdentifierException e)
        {
            return null;
        }
    }

    private static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i == 0 && (c == '-' || c == '+')) continue;
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public static float getHardness(BlockPos pos) {
        return getState(pos).getHardness(MC.world, pos);
    }

    public static boolean isUnbreakable(BlockPos pos) {
        return getState(pos).getHardness(MC.world, pos) < 0;
    }

    public static VoxelShape getOutlineShape(BlockPos pos)
    {
        return getState(pos).getOutlineShape(MC.world, pos);
    }

    public static Box getBoundingBox(BlockPos pos)
    {
        return getOutlineShape(pos).getBoundingBox().offset(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean canBeClicked(BlockPos pos)
    {
        return getOutlineShape(pos) != VoxelShapes.empty();
    }

    public static boolean isOpaqueFullCube(BlockPos pos) {
        return getState(pos).isOpaque();
    }

    public static BlockHitResult raycast(Vec3d from, Vec3d to, RaycastContext.FluidHandling fluidHandling)
    {
        RaycastContext context = new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, fluidHandling, MC.player);

        return MC.world.raycast(context);
    }

    public static BlockHitResult raycast(Vec3d from, Vec3d to)
    {
        return raycast(from, to, RaycastContext.FluidHandling.NONE);
    }

    public static boolean hasLineOfSight(Vec3d from, Vec3d to)
    {
        return raycast(from, to).getType() == HitResult.Type.MISS;
    }

    public static boolean hasLineOfSight(Vec3d to) {
        return raycast(MC.player.getCameraPosVec(1.0f), to).getType() == HitResult.Type.MISS;
    }

    /**
     * Returns a stream of all blocks that collide with the given box.
     * Only returns bounding boxes that intersect the given box.
     */
    public static Stream<Box> getBlockCollisions(Box box)
    {
        Iterable<VoxelShape> blockCollisions = MC.world.getBlockCollisions(MC.player, box);

        return StreamSupport.stream(blockCollisions.spliterator(), false)
                .flatMap(shape -> shape.getBoundingBoxes().stream())
                .filter(shapeBox -> shapeBox.intersects(box));
    }

    public static ArrayList<BlockPos> getAllInBox(BlockPos from, BlockPos to)
    {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

        for (int x = min.getX(); x <= max.getX(); x++)
            for (int y = min.getY(); y <= max.getY(); y++)
                for (int z = min.getZ(); z <= max.getZ(); z++)
                    blocks.add(new BlockPos(x, y, z));

        return blocks;
    }

    public static ArrayList<BlockPos> getAllInBox(BlockPos center, int range)
    {
        return getAllInBox(center.add(-range, -range, -range),
                center.add(range, range, range));
    }

    public static Stream<BlockPos> getAllInBoxStream(BlockPos from, BlockPos to)
    {
        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

        Stream<BlockPos> stream = Stream.<BlockPos>iterate(min, pos -> {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            x++;

            if (x > max.getX())
            {
                x = min.getX();
                y++;
            }

            if (y > max.getY())
            {
                y = min.getY();
                z++;
            }

            if (z > max.getZ())
                throw new IllegalStateException("Stream limit exceeded.");

            return new BlockPos(x, y, z);
        });

        int limit = (max.getX() - min.getX() + 1)
                * (max.getY() - min.getY() + 1)
                * (max.getZ() - min.getZ() + 1);

        return stream.limit(limit);
    }

    public static Stream<BlockPos> getAllInBoxStream(BlockPos center, int range)
    {
        return getAllInBoxStream(center.add(-range, -range, -range),
                center.add(range, range, range));
    }
}
