package com.medievalfactions.block.model;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFArcaneWorkbenchBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MFArcaneWorkbenchBlockModel extends GeoModel<MFArcaneWorkbenchBlockEntity> {
    @Override public ResourceLocation getModelResource(MFArcaneWorkbenchBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "geo/blocks/arcane_workbench.geo.json"); }
    @Override public ResourceLocation getTextureResource(MFArcaneWorkbenchBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/entity/arcane_workbench.png"); }
    @Override public ResourceLocation getAnimationResource(MFArcaneWorkbenchBlockEntity animatable) { return ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "animations/blocks/medieval_blocks.animation.json"); }
}
