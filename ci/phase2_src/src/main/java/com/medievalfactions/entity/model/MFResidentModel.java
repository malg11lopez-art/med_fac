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
        String texture = switch (animatable.appearanceId()) {
            case 1 -> "resident_farmer.png";
            case 2 -> "resident_smith.png";
            case 3 -> "resident_mage.png";
            case 4 -> "resident_trader.png";
            default -> "resident.png";
        };
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/" + texture);
    }

    @Override
    public ResourceLocation getAnimationResource(MFResidentEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/entities/guard_full.animation.json");
    }
}
