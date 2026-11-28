package com.medievalfactions.block.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFForgeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFForgeBlockModel extends GeoModel<MFForgeBlockEntity> {
    @Override public ResourceLocation getModelResource(MFForgeBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/blocks/forge.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFForgeBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/forge.png"); }
    @Override public ResourceLocation getAnimationResource(MFForgeBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/blocks/medieval_blocks.animation.json"); }
}
