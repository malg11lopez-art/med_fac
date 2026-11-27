package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFResidentEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Civilian resident appearance on the shared humanoid rig; guards retain their armored texture. */
public final class MFResidentModel extends GeoModel<MFResidentEntity> {
    @Override
    public ResourceLocation getModelResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/guard.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/resident.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/guard_full.animation.json");
    }
}
