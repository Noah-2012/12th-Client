package com.noadsch12;

import com.noadsch12.networking.ClientStatusPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwelfthMain implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");



    @Override
    public void onInitialize() {
        LOGGER.info("12th Client - Main initialized!");
            // Register the payload
        PayloadTypeRegistry.playC2S().register(ClientStatusPayload.ID, ClientStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientStatusPayload.ID, ClientStatusPayload.CODEC);

            // Notify everyone when a user joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ClientStatusPayload joinPacket = new ClientStatusPayload(handler.player.getUuid());
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, joinPacket); // Tell others about me
                ServerPlayNetworking.send(handler.player, new ClientStatusPayload(player.getUuid())); // Tell me about others
            }
        });
    }
}