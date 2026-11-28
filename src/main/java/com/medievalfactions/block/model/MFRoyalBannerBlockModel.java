package com.medievalfactions.block.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFRoyalBannerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFRoyalBannerBlockModel extends GeoModel<MFRoyalBannerBlockEntity> {
    @Override public ResourceLocation getModelResource(MFRoyalBannerBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/blocks/royal_banner.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFRoyalBannerBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/royal_banner.png"); }
    @Override public ResourceLocation getAnimationResource(MFRoyalBannerBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/blocks/medieval_blocks.animation.json"); }
}
