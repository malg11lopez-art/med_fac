package com.medievalfactions.block.entity;

import com.medievalfactions.registry.MFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public final class MFForgeBlockEntity extends MFAnimatedBlockEntity {
    public MFForgeBlockEntity(BlockPos pos, BlockState state) { super(MFBlockEntities.FORGE.get(), pos, state); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "primary", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.forge.embers"))));
        controllers.add(new AnimationController<>(this, "secondary", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.forge.bellows"))));
    }
}
