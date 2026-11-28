package com.medievalfactions.entity.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.MFResidentEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Profession-tinted civilian textures share the resident humanoid geometry and animation rig. */
public final class MFResidentModel extends GeoModel<MFResidentEntity> {
    @Override
    public ResourceLocation getModelResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/entities/guard.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MFResidentEntity animatable) {
        String texture = switch (animatable.visualRole()) {
            case 1 -> "resident_farmer";
            case 2 -> "resident_smith";
            case 3 -> "resident_scholar";
            case 4 -> "resident_healer";
            case 5 -> "resident_trader";
            case 6 -> "resident_guard";
            default -> "resident";
        };
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID,
                "textures/entity/" + texture + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/guard_full.animation.json");
    }
}
