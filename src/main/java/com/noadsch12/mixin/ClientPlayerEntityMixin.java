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
import com.noadsch12.handlers.AutoArmor;
import com.noadsch12.handlers.AutoRefill;
import com.noadsch12.handlers.AutoTool;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ModuleManager.getInstance().getModule("Auto Armor").isEnabled()) AutoArmor.tick();
        if (ModuleManager.getInstance().getModule("Auto Refill").isEnabled()) AutoRefill.tick();
        if (ModuleManager.getInstance().getModule("Auto Tool").isEnabled()) AutoTool.tick();
    }

    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getInstance().getModule("Freecam").isEnabled()) {
            cir.setReturnValue(true);
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
