package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFHarpyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFHarpyModel extends GeoModel<MFHarpyEntity> {
    @Override public ResourceLocation getModelResource(MFHarpyEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/harpy.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFHarpyEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/harpy.png"); }
    @Override public ResourceLocation getAnimationResource(MFHarpyEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/special_entities.animation.json"); }
}
