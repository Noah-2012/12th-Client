package com.noadsch12.cheats;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool {

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only switch if the player is actually holding down the "Attack/Mine" button
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            handleBlockMining(mc, ((BlockHitResult) hit));
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            handleEntityAttack(mc, ((EntityHitResult) hit).getEntity());
        }
    }

    private static void handleBlockMining(MinecraftClient mc, BlockHitResult hit) {
        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.isAir()) return;

        int bestSlot = -1;
        float bestSpeed = mc.player.getMainHandStack().getMiningSpeedMultiplier(state);

        // Scan the hotbar first (0-8) for the best speed
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private static void handleEntityAttack(MinecraftClient mc, Entity target) {
        if (!(target instanceof LivingEntity)) return;

        int bestSlot = -1;
        // Get base damage of current hand
        double bestDamage = mc.player.getMainHandStack().getDamage();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            double damage = stack.getDamage();
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }
}