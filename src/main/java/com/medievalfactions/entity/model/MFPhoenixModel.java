package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFPhoenixEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFPhoenixModel extends GeoModel<MFPhoenixEntity> {
    @Override public ResourceLocation getModelResource(MFPhoenixEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/phoenix.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFPhoenixEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/phoenix.png"); }
    @Override public ResourceLocation getAnimationResource(MFPhoenixEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
