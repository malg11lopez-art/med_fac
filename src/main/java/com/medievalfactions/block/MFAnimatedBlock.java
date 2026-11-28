package com.medievalfactions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Supplier;

public final class MFAnimatedBlock extends Block implements EntityBlock {
    private final Supplier<? extends BlockEntityType<? extends BlockEntity>> blockEntityType;
    public MFAnimatedBlock(BlockBehaviour.Properties properties, Supplier<? extends BlockEntityType<? extends BlockEntity>> type) {
        super(properties);
        this.blockEntityType = type;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.get().create(pos, state);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}
