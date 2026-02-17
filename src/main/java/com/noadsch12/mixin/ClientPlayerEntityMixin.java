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

import com.noadsch12.mixin.interfaces.ICamera;
import com.noadsch12.modules.ModuleManager;
import com.noadsch12.modules.impl.player.FreecamModule;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import com.noadsch12.cheats.AutoArmor;
import com.noadsch12.cheats.AutoRefill;
import com.noadsch12.cheats.AutoTool;
import com.noadsch12.cheats.AutoTotem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ModuleManager.getInstance().getModule("Auto Totem").isEnabled()) AutoTotem.tick();
        if (ModuleManager.getInstance().getModule("Auto Armor").isEnabled()) AutoArmor.tick();
        if (ModuleManager.getInstance().getModule("Auto Refill").isEnabled()) AutoRefill.tick();
        if (ModuleManager.getInstance().getModule("Auto Tool").isEnabled()) AutoTool.tick();
    }

    @Override
    public void onIsSpectator(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getInstance().getModule("Freecam").isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getInstance().getModule("Freecam").isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void onSetHealth(float health, CallbackInfo ci) {
        /*
        PlayerHealthEvent event = new PlayerHealthEvent(null, health);
        Aoba.getInstance().eventManager.Fire(event);

         */
    }

    @Override
    protected void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
        /*
        if (Aoba.getInstance().moduleManager.fly.state.getValue()) {
            Fly fly = Aoba.getInstance().moduleManager.fly;
            cir.setReturnValue((float) fly.getSpeed());
        } else if (Aoba.getInstance().moduleManager.noclip.state.getValue()) {
            Noclip noclip = Aoba.getInstance().moduleManager.noclip;
            cir.setReturnValue(noclip.getSpeed());
        }

         */
    }

    @Override
    public void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
        /*
        Step stepHack = Aoba.getInstance().moduleManager.step;
        if (stepHack.state.getValue()) {
            cir.setReturnValue(cir.getReturnValue());
        }

         */
    }

    @Override
    public void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        /*
        AobaClient aoba = Aoba.getInstance();
        HighJump higherJump = aoba.moduleManager.higherjump;
        if (higherJump.state.getValue()) {
            cir.setReturnValue(higherJump.getJumpHeightMultiplier());
        }

         */
    }

    @Override
    public void onTickNewAi(CallbackInfo ci) {
        if (ModuleManager.getInstance().getModule("Freecam").isEnabled())
            ci.cancel();
    }

    @Override
    public void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        FreecamModule freecam = ModuleManager.getInstance().getModule(FreecamModule.class);

        if (freecam.isEnabled()) {
            // Adjust sensitivity (0.15 is standard)
            float sensitivity = 0.15f;
            float newYaw = freecam.getYaw() + (float) cursorDeltaX * sensitivity;
            float newPitch = freecam.getPitch() + (float) cursorDeltaY * sensitivity;

            // Clamp pitch to prevent flipping upside down
            newPitch = Math.max(-90.0f, Math.min(90.0f, newPitch));

            freecam.setRotation(newYaw, newPitch);
            ci.cancel(); // Stop the real player from turning
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        /*
        SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
        Aoba.getInstance().eventManager.Fire(sendMovementPacketPreEvent);

         */
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPacketsHead(CallbackInfo info) {
        /*
        SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
        Aoba.getInstance().eventManager.Fire(sendMovementPacketPreEvent);
        if (sendMovementPacketPreEvent.isCancelled())
            info.cancel();

         */
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"), cancellable = true)
    private void onSendMovementPacketsTail(CallbackInfo info) {
        /*
        SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();
        Aoba.getInstance().eventManager.Fire(sendMovementPacketPostEvent);
        if (sendMovementPacketPostEvent.isCancelled())
            info.cancel();

         */
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        /*
        SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();

        Aoba.getInstance().eventManager.Fire(sendMovementPacketPostEvent);

         */
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        /*
        AntiKnockback antiKnockback = Aoba.getInstance().moduleManager.antiknockback;

        if (antiKnockback.state.getValue() && antiKnockback.getNoPushBlocks()) {
            ci.cancel();
        }

         */
    }
}
