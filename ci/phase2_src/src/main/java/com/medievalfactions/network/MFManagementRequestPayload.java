package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

/** Client request for one bounded, server-authoritative management dashboard page. */
public record MFManagementRequestPayload(int page) implements CustomPacketPayload {
    public static final int PAGE_COUNT = 8;
    public static final Type<MFManagementRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "management_request"));
    public static final StreamCodec<ByteBuf, MFManagementRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MFManagementRequestPayload::page,
            MFManagementRequestPayload::new);

    public MFManagementRequestPayload {
        if (page < 0 || page >= PAGE_COUNT) throw new IllegalArgumentException("Invalid management page");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
