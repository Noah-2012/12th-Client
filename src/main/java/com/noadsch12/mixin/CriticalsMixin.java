package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayerInteractionManager.class)
public class CriticalsMixin {

    @Inject(
            method = "attackEntity",
            at = @At("HEAD")
    )
    private void sendFakeCriticalPackets(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (player == null || target == null) return;
        if (!ClientSettingsScreen.CriticalsEnabled) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // Classic critical spoof sequence
        send(x, y + 0.0625D, z);
        send(x, y, z);
        send(x, y + 0.0125D, z);
        send(x, y, z);
    }

    @Unique
    private void send(double x, double y, double z) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
                .sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y, z, false, false
                ));
    }
}
