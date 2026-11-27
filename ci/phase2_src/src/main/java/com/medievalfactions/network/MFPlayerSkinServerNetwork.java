package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.data.MFManagementDashboard;
import com.medievalfactions.network.MFManagementRequestPayload;
import com.medievalfactions.network.MFManagementSnapshotPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

/** Server-owned skin persistence and validated network synchronization. */
@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MFPlayerSkinServerNetwork {
    private static final Logger LOGGER = LogUtils.getLogger();
    private MFPlayerSkinServerNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MFPlayerSkinSelectionPayload.TYPE,
                MFPlayerSkinSelectionPayload.STREAM_CODEC,
                MFPlayerSkinServerNetwork::handleSelection);
        registrar.playToServer(MFManagementRequestPayload.TYPE,
                MFManagementRequestPayload.STREAM_CODEC,
                MFPlayerSkinServerNetwork::handleManagementRequest);

        // Register server-to-client payloads on the server as well. NeoForge
        // negotiates both directions during login; omitting these declarations
        // disconnects otherwise-compatible clients before they can join.
        registrar.playToClient(MFPlayerSkinSyncPayload.TYPE,
                MFPlayerSkinSyncPayload.STREAM_CODEC,
                (payload, context) -> {});
        registrar.playToClient(MFManagementSnapshotPayload.TYPE,
                MFManagementSnapshotPayload.STREAM_CODEC,
                (payload, context) -> {});
    }

    private static void handleManagementRequest(MFManagementRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            int page = payload.page();
            String body = MFManagementDashboard.render(player, page);
            PacketDistributor.sendToPlayer(player,
                    new MFManagementSnapshotPayload(page, MFManagementDashboard.title(page), body));
        });
    }

    private static void handleSelection(MFPlayerSkinSelectionPayload payload, IPayloadContext context) {
        // Payload handlers may be invoked from the network thread. World/player state
        // and PacketDistributor must be touched from the logical server thread.
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            int index = payload.skinIndex();
            if (!MFPlayerSkinRules.isValidIndex(index)) return;

            var data = player.getPersistentData();
            long now = player.serverLevel().getGameTime();
            if (data.contains(MFPlayerSkinRules.CHANGE_TICK_KEY, Tag.TAG_LONG)) {
                long previous = data.getLong(MFPlayerSkinRules.CHANGE_TICK_KEY);
                // A server/world time reset must not permanently lock a player out.
                if (now >= previous && now - previous < 3L) return;
            }

            data.putLong(MFPlayerSkinRules.CHANGE_TICK_KEY, now);
            data.putInt(MFPlayerSkinRules.DATA_KEY, index);
            PacketDistributor.sendToAllPlayers(new MFPlayerSkinSyncPayload(player.getUUID(), index));
            LOGGER.info("MF_SKIN_SELECTION_ACCEPTED player={} index={}", player.getGameProfile().getName(), index);
        });
    }

}
