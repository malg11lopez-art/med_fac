package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFHorseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFHorseModel extends GeoModel<MFHorseEntity> {
    @Override public ResourceLocation getModelResource(MFHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/horse.geo.json");
    }
    @Override public ResourceLocation getTextureResource(MFHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/horse.png");
    }
    @Override public ResourceLocation getAnimationResource(MFHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/horse.animation.json");
    }
}
