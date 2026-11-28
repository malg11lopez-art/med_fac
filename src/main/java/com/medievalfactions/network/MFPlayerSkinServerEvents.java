package com.medievalfactions.network;

import com.medievalfactions.MedievalFactions;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Lifecycle events for persistent player cosmetics; these run on the GAME bus. */
@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MFPlayerSkinServerEvents {
    private MFPlayerSkinServerEvents() {}

    /** A joining client receives the persisted cosmetic for every player already online. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer joining)) return;
        for (ServerPlayer online : joining.serverLevel().getServer().getPlayerList().getPlayers()) {
            var data = online.getPersistentData();
            if (data.contains(MFPlayerSkinRules.DATA_KEY, Tag.TAG_INT)
                    && MFPlayerSkinRules.isValidIndex(data.getInt(MFPlayerSkinRules.DATA_KEY))) {
                PacketDistributor.sendToPlayer(joining,
                        new MFPlayerSkinSyncPayload(online.getUUID(), data.getInt(MFPlayerSkinRules.DATA_KEY)));
            }
        }
    }

    /** Remove the remote cosmetic cache entry after a player disconnects. */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToAllPlayers(new MFPlayerSkinSyncPayload(player.getUUID(), -1));
        }
    }

    /** Respawned players retain their saved skin and republish it to all connected clients. */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        if (data.contains(MFPlayerSkinRules.DATA_KEY, Tag.TAG_INT)
                && MFPlayerSkinRules.isValidIndex(data.getInt(MFPlayerSkinRules.DATA_KEY))) {
            PacketDistributor.sendToAllPlayers(new MFPlayerSkinSyncPayload(
                    player.getUUID(), data.getInt(MFPlayerSkinRules.DATA_KEY)));
        }
    }

    /** Copy the cosmetic across death cloning on NeoForge versions that create a fresh player object. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var source = event.getOriginal().getPersistentData();
        if (source.contains(MFPlayerSkinRules.DATA_KEY, Tag.TAG_INT)
                && MFPlayerSkinRules.isValidIndex(source.getInt(MFPlayerSkinRules.DATA_KEY))) {
            event.getEntity().getPersistentData().putInt(MFPlayerSkinRules.DATA_KEY,
                    source.getInt(MFPlayerSkinRules.DATA_KEY));
        }
    }
}
