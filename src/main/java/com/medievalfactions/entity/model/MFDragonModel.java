package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFDragonModel extends GeoModel<MFDragonEntity> {
    @Override public ResourceLocation getModelResource(MFDragonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/dragon.geo.json");
    }
    @Override public ResourceLocation getTextureResource(MFDragonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/dragon.png");
    }
    @Override public ResourceLocation getAnimationResource(MFDragonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/dragon.animation.json");
    }
}
