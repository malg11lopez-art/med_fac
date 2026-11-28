package com.medievalfactions.registry;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.entity.MFForgeBlockEntity;
import com.medievalfactions.block.entity.MFArcaneWorkbenchBlockEntity;
import com.medievalfactions.block.entity.MFRoyalBannerBlockEntity;
import com.medievalfactions.block.entity.MFWindmillBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class MFBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MedievalFactions.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MFForgeBlockEntity>> FORGE = BLOCK_ENTITY_TYPES.register("forge_controller",
        () -> BlockEntityType.Builder.of(MFForgeBlockEntity::new, MFBlocks.FORGE_CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MFArcaneWorkbenchBlockEntity>> ARCANE_WORKBENCH = BLOCK_ENTITY_TYPES.register("arcane_workbench",
        () -> BlockEntityType.Builder.of(MFArcaneWorkbenchBlockEntity::new, MFBlocks.ARCANE_WORKBENCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MFRoyalBannerBlockEntity>> ROYAL_BANNER = BLOCK_ENTITY_TYPES.register("royal_banner",
        () -> BlockEntityType.Builder.of(MFRoyalBannerBlockEntity::new, MFBlocks.ROYAL_BANNERS.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MFWindmillBlockEntity>> WINDMILL = BLOCK_ENTITY_TYPES.register("windmill",
        () -> BlockEntityType.Builder.of(MFWindmillBlockEntity::new, MFBlocks.WINDMILL.get()).build(null));
    private MFBlockEntities() {}
}
