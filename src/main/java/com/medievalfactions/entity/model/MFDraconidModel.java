package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFDraconidEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFDraconidModel extends GeoModel<MFDraconidEntity> {
    @Override public ResourceLocation getModelResource(MFDraconidEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/draconid.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFDraconidEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/draconid.png"); }
    @Override public ResourceLocation getAnimationResource(MFDraconidEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
