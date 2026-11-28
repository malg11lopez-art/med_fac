package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

/** Client request to change the sender's server-authoritative player cosmetic. */
public record MFPlayerSkinSelectionPayload(int skinIndex) implements CustomPacketPayload {
    public static final Type<MFPlayerSkinSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "player_skin_select"));

    public static final StreamCodec<ByteBuf, MFPlayerSkinSelectionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MFPlayerSkinSelectionPayload::skinIndex,
            MFPlayerSkinSelectionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
