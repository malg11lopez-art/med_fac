package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFGriffinEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFGriffinModel extends GeoModel<MFGriffinEntity> {
    @Override public ResourceLocation getModelResource(MFGriffinEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/griffin.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFGriffinEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/griffin.png"); }
    @Override public ResourceLocation getAnimationResource(MFGriffinEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
