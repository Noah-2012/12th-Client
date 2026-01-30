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