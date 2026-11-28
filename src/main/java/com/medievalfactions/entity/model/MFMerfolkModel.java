package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFMerfolkEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFMerfolkModel extends GeoModel<MFMerfolkEntity> {
    @Override public ResourceLocation getModelResource(MFMerfolkEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/merfolk.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFMerfolkEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/merfolk.png"); }
    @Override public ResourceLocation getAnimationResource(MFMerfolkEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
