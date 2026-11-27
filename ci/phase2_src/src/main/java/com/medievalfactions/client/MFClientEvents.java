package com.medievalfactions.client;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFForgeBlockEntity;
import com.medievalfactions.block.entity.MFArcaneWorkbenchBlockEntity;
import com.medievalfactions.block.entity.MFRoyalBannerBlockEntity;
import com.medievalfactions.block.entity.MFWindmillBlockEntity;
import com.medievalfactions.block.model.MFForgeBlockModel;
import com.medievalfactions.block.model.MFArcaneWorkbenchBlockModel;
import com.medievalfactions.block.model.MFRoyalBannerBlockModel;
import com.medievalfactions.block.model.MFWindmillBlockModel;
import com.medievalfactions.entity.MFGuardEntity;
import com.medievalfactions.entity.MFResidentEntity;
import com.medievalfactions.entity.model.MFResidentModel;
import com.medievalfactions.entity.MFHorseEntity;
import com.medievalfactions.entity.MFDragonEntity;
import com.medievalfactions.entity.MFCentaurEntity;
import com.medievalfactions.entity.MFHarpyEntity;
import com.medievalfactions.entity.MFMerfolkEntity;
import com.medievalfactions.entity.MFDraconidEntity;
import com.medievalfactions.entity.MFWolfEntity;
import com.medievalfactions.entity.MFGriffinEntity;
import com.medievalfactions.entity.MFPhoenixEntity;
import com.medievalfactions.entity.model.MFCentaurModel;
import com.medievalfactions.entity.model.MFHarpyModel;
import com.medievalfactions.entity.model.MFMerfolkModel;
import com.medievalfactions.entity.model.MFDraconidModel;
import com.medievalfactions.entity.model.MFWolfModel;
import com.medievalfactions.entity.model.MFGriffinModel;
import com.medievalfactions.entity.model.MFPhoenixModel;
import com.medievalfactions.entity.model.MFGuardModel;
import com.medievalfactions.entity.model.MFHorseModel;
import com.medievalfactions.entity.model.MFDragonModel;
import com.medievalfactions.registry.MFBlockEntities;
import com.medievalfactions.registry.MFEntities;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.medievalfactions.network.MFPlayerSkinSyncPayload;
import com.medievalfactions.network.MFManagementSnapshotPayload;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MFClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(MFClientEvents.class);
    private MFClientEvents() {}
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MFPlayerSkinInput.CYCLE_SKIN);
        event.register(MFPlayerSkinInput.OPEN_MENU);
    }

    @SubscribeEvent
    public static void registerPlayerSkinPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(MFPlayerSkinSyncPayload.TYPE,
                MFPlayerSkinSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> MFPlayerSkinClient.setRemoteSkin(payload.playerId(), payload.skinIndex())));
        event.registrar("1").playToClient(MFManagementSnapshotPayload.TYPE,
                MFManagementSnapshotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> MFManagementClientState.accept(payload)));
    }

    @SubscribeEvent
    public static void addPlayerSkinLayers(EntityRenderersEvent.AddLayers event) {
        int installed = 0;
        for (net.minecraft.client.resources.PlayerSkin.Model skinModel : event.getSkins()) {
            if (event.getSkin(skinModel) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new MFPlayerSkinLayer(renderer));
                installed++;
            }
        }
        LOGGER.info("Registered MEDIEVAL FACTIONS player skin layer on {} renderer variants; {} local skins available",
                installed, MFPlayerSkinClient.skinCount());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MFEntities.GUARD.get(), ctx -> new GeoEntityRenderer<MFGuardEntity>(ctx, new MFGuardModel()));
        event.registerEntityRenderer(MFEntities.RESIDENT.get(), ctx -> new GeoEntityRenderer<MFResidentEntity>(ctx, new MFResidentModel()));
        event.registerEntityRenderer(MFEntities.HORSE.get(), ctx -> new GeoEntityRenderer<MFHorseEntity>(ctx, new MFHorseModel()));
        event.registerEntityRenderer(MFEntities.DRAGON.get(), ctx -> new GeoEntityRenderer<MFDragonEntity>(ctx, new MFDragonModel()));
        event.registerEntityRenderer(MFEntities.CENTAUR.get(), ctx -> new GeoEntityRenderer<MFCentaurEntity>(ctx, new MFCentaurModel()));
        event.registerEntityRenderer(MFEntities.HARPY.get(), ctx -> new GeoEntityRenderer<MFHarpyEntity>(ctx, new MFHarpyModel()));
        event.registerEntityRenderer(MFEntities.MERFOLK.get(), ctx -> new GeoEntityRenderer<MFMerfolkEntity>(ctx, new MFMerfolkModel()));
        event.registerEntityRenderer(MFEntities.DRACONID.get(), ctx -> new GeoEntityRenderer<MFDraconidEntity>(ctx, new MFDraconidModel()));
        event.registerEntityRenderer(MFEntities.WOLF.get(), ctx -> new GeoEntityRenderer<MFWolfEntity>(ctx, new MFWolfModel()));
        event.registerEntityRenderer(MFEntities.GRIFFIN.get(), ctx -> new GeoEntityRenderer<MFGriffinEntity>(ctx, new MFGriffinModel()));
        event.registerEntityRenderer(MFEntities.PHOENIX.get(), ctx -> new GeoEntityRenderer<MFPhoenixEntity>(ctx, new MFPhoenixModel()));
    }
    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MFBlockEntities.FORGE.get(), ctx -> new GeoBlockRenderer<MFForgeBlockEntity>(new MFForgeBlockModel()));
        event.registerBlockEntityRenderer(MFBlockEntities.ARCANE_WORKBENCH.get(), ctx -> new GeoBlockRenderer<MFArcaneWorkbenchBlockEntity>(new MFArcaneWorkbenchBlockModel()));
        event.registerBlockEntityRenderer(MFBlockEntities.ROYAL_BANNER.get(), ctx -> new GeoBlockRenderer<MFRoyalBannerBlockEntity>(new MFRoyalBannerBlockModel()));
        event.registerBlockEntityRenderer(MFBlockEntities.WINDMILL.get(), ctx -> new GeoBlockRenderer<MFWindmillBlockEntity>(new MFWindmillBlockModel()));
    }
}
