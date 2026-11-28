package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFWolfEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFWolfModel extends GeoModel<MFWolfEntity> {
    @Override public ResourceLocation getModelResource(MFWolfEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/wolf.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFWolfEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/wolf.png"); }
    @Override public ResourceLocation getAnimationResource(MFWolfEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
