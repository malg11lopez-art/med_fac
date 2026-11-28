package com.medievalfactions.registry;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.block.MFAnimatedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.core.registries.Registries;

public final class MFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MedievalFactions.MOD_ID);

    public static final DeferredBlock<Block> BLACKSMITH_ANVIL = BLOCKS.register("blacksmith_anvil",
        () -> new Block(BlockBehaviour.Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TOWN_HALL_BRICK = BLOCKS.register("town_hall_brick",
        () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 6.0F).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> FORGE_CONTROLLER = BLOCKS.register("forge_controller",
        () -> new MFAnimatedBlock(BlockBehaviour.Properties.of().strength(4.0F, 6.0F), () -> MFBlockEntities.FORGE.get()));
    public static final DeferredBlock<Block> ARCANE_WORKBENCH = BLOCKS.register("arcane_workbench",
        () -> new MFAnimatedBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F), () -> MFBlockEntities.ARCANE_WORKBENCH.get()));
    public static final DeferredBlock<Block> ROYAL_BANNERS = BLOCKS.register("royal_banners",
        () -> new MFAnimatedBlock(BlockBehaviour.Properties.of().strength(1.0F), () -> MFBlockEntities.ROYAL_BANNER.get()));
    public static final DeferredBlock<Block> WINDMILL = BLOCKS.register("windmill",
        () -> new MFAnimatedBlock(BlockBehaviour.Properties.of().strength(2.0F), () -> MFBlockEntities.WINDMILL.get()));
    public static final DeferredBlock<Block> SUNSTEEL_ORE = BLOCKS.register("sunsteel_ore",
        () -> new Block(BlockBehaviour.Properties.of().strength(3.5F, 3.0F).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_SUNSTEEL_ORE = BLOCKS.register("deepslate_sunsteel_ore",
        () -> new Block(BlockBehaviour.Properties.of().strength(4.5F, 3.0F).requiresCorrectToolForDrops()));

    private MFBlocks() {}
}
