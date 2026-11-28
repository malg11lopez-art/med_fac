package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFCentaurEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFCentaurModel extends GeoModel<MFCentaurEntity> {
    @Override public ResourceLocation getModelResource(MFCentaurEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/centaur.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFCentaurEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/centaur.png"); }
    @Override public ResourceLocation getAnimationResource(MFCentaurEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
