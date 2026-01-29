package com.noadsch12.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

public record ClientStatusPayload(UUID playerUuid) implements CustomPayload {
    public static final Id<ClientStatusPayload> ID = new Id<>(Identifier.of("12th-client", "client_status"));
    public static final PacketCodec<RegistryByteBuf, ClientStatusPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, ClientStatusPayload::playerUuid, ClientStatusPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}