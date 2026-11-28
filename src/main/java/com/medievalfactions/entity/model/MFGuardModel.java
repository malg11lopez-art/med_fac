package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFGuardEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFGuardModel extends GeoModel<MFGuardEntity> {
    @Override public ResourceLocation getModelResource(MFGuardEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/guard.geo.json");
    }
    @Override public ResourceLocation getTextureResource(MFGuardEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/guard.png");
    }
    @Override public ResourceLocation getAnimationResource(MFGuardEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/guard_full.animation.json");
    }
}
