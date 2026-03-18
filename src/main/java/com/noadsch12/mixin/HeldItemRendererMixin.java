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

package com.noadsch12.mixin;


import com.noadsch12.modules.ModuleManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow @Final
    private ItemStack mainHand;
    @Shadow @Final private ItemStack offHand;

    @Shadow
    protected void renderMapInBothHands(MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, float pitch, float equipProgress, float swingProgress) {

    }

    @Shadow
    protected void renderMapInOneHand(MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack) {}

    @Shadow
    protected void renderArmHoldingItem(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float equipProgress, float swingProgress, Arm arm) {

    }

    @Shadow
    protected void applyEatOrDrinkTransformation(MatrixStack matrices, float tickProgress, Arm arm, ItemStack stack, net.minecraft.entity.player.PlayerEntity player) {

    }

    @Shadow
    protected void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {

    }

    @Shadow
    public void renderItem(net.minecraft.entity.LivingEntity entity, ItemStack stack, net.minecraft.item.ItemDisplayContext renderMode, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light) {

    }

    /**
     * @author Noadsch12
     * @reason Overwriting to implement spear-style thrusting and custom positioning.
     */
    @Overwrite
    private void renderFirstPersonItem(
            AbstractClientPlayerEntity player,
            float tickProgress,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            int light
    ) {
        if (!ModuleManager.getInstance().getModule("Item Rotation").isEnabled()) {
            return;
        }

        if (player.isUsingSpyglass()) return;

        boolean isMainHand = hand == Hand.MAIN_HAND;
        Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        matrices.push();

        if (item.isEmpty()) {
            if (isMainHand && !player.isInvisible()) {
                this.renderArmHoldingItem(matrices, orderedRenderCommandQueue, light, equipProgress, swingProgress, arm);
            }
        } else if (item.contains(DataComponentTypes.MAP_ID)) {
            if (isMainHand && this.offHand.isEmpty()) {
                this.renderMapInBothHands(matrices, orderedRenderCommandQueue, light, pitch, equipProgress, swingProgress);
            } else {
                this.renderMapInOneHand(matrices, orderedRenderCommandQueue, light, equipProgress, arm, swingProgress, item);
            }
        } else {
            boolean isRightArm = arm == Arm.RIGHT;

            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                this.applyEatOrDrinkTransformation(matrices, tickProgress, arm, item, player);
                this.applyEquipOffset(matrices, arm, equipProgress);
            } else {
                // 1. Still apply equip offset so the item slides up/down when switching
                this.applyEquipOffset(matrices, arm, equipProgress);

                // 2. THE ANIMATION ONLY
                // Calculate the thrusting motion (sine wave)
                float thrust = MathHelper.sin(swingProgress * (float)Math.PI);

                // 3. Apply ONLY the forward movement
                // This will add to the Z-position handled by your other Mixin
                matrices.translate(0.0f, 0.0f, -(thrust * 0.7f));

                // Optional: A tiny bit of rotation during the hit to make it feel "impactful"
                // matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(thrust * -5.0f));
            }

            this.renderItem(
                    player,
                    item,
                    isRightArm ? net.minecraft.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : net.minecraft.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                    matrices,
                    orderedRenderCommandQueue,
                    light
            );
        }

        matrices.pop();
    }
}
