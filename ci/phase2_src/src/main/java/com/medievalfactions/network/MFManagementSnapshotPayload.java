package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

/** Bounded text snapshot sent only to the player who requested a dashboard page. */
public record MFManagementSnapshotPayload(int page, String title, String body) implements CustomPacketPayload {
    public static final int MAX_TITLE_CHARS = 80;
    public static final int MAX_BODY_CHARS = 3_600;
    public static final Type<MFManagementSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "management_snapshot"));
    public static final StreamCodec<ByteBuf, MFManagementSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MFManagementSnapshotPayload::page,
            ByteBufCodecs.STRING_UTF8, MFManagementSnapshotPayload::title,
            ByteBufCodecs.STRING_UTF8, MFManagementSnapshotPayload::body,
            MFManagementSnapshotPayload::new);

    public MFManagementSnapshotPayload {
        if (page < 0 || page >= MFManagementRequestPayload.PAGE_COUNT) throw new IllegalArgumentException("Invalid page");
        title = title == null ? "MEDIEVAL FACTIONS" : title.strip();
        body = body == null ? "" : body;
        if (title.length() > MAX_TITLE_CHARS || body.length() > MAX_BODY_CHARS)
            throw new IllegalArgumentException("Management snapshot exceeds packet limit");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
