package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.data.MFManagementDashboard;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
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

        // A dedicated server has no client event subscriber, so it must declare
        // these outbound channels with no-op local handlers for negotiation.
        // In a client JVM, MFClientEvents registers the same payload IDs with
        // their real client handlers; registering them here as well would make
        // NeoForge reject the duplicate during startup (including integrated play).
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            registrar.playToClient(MFPlayerSkinSyncPayload.TYPE,
                    MFPlayerSkinSyncPayload.STREAM_CODEC,
                    (payload, context) -> {});
            registrar.playToClient(MFManagementSnapshotPayload.TYPE,
                    MFManagementSnapshotPayload.STREAM_CODEC,
                    (payload, context) -> {});
        }
    }

    private static void handleManagementRequest(MFManagementRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            int page = payload.page();
            var persistent = player.getPersistentData();
            long now = player.serverLevel().getGameTime();
            if (persistent.contains(MFManagementAccessRules.LAST_REQUEST_TICK_KEY, Tag.TAG_LONG)
                    && !MFManagementAccessRules.mayRequest(now,
                    persistent.getLong(MFManagementAccessRules.LAST_REQUEST_TICK_KEY))) return;
            persistent.putLong(MFManagementAccessRules.LAST_REQUEST_TICK_KEY, now);

            if (!MFManagementAccessRules.canViewGlobalDashboard(player.hasPermissions(2))) {
                PacketDistributor.sendToPlayer(player, new MFManagementSnapshotPayload(page,
                        "Acceso restringido",
                        "El panel actual muestra datos globales del mundo y requiere permisos de operador (nivel 2)."));
                return;
            }
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
