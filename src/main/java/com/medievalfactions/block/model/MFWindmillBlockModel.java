package com.medievalfactions.block.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFWindmillBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFWindmillBlockModel extends GeoModel<MFWindmillBlockEntity> {
    @Override public ResourceLocation getModelResource(MFWindmillBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/blocks/windmill.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFWindmillBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/windmill.png"); }
    @Override public ResourceLocation getAnimationResource(MFWindmillBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/blocks/medieval_blocks.animation.json"); }
}
