package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

/** Server broadcast describing a player's persistent skin choice. */
public record MFPlayerSkinSyncPayload(UUID playerId, int skinIndex) implements CustomPacketPayload {
    public static final Type<MFPlayerSkinSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "player_skin_sync"));

    public static final StreamCodec<ByteBuf, MFPlayerSkinSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MFPlayerSkinSyncPayload::playerId,
            ByteBufCodecs.VAR_INT, MFPlayerSkinSyncPayload::skinIndex,
            MFPlayerSkinSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
