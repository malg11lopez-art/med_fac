package com.medievalfactions.block.entity;

import com.medievalfactions.registry.MFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public final class MFArcaneWorkbenchBlockEntity extends MFAnimatedBlockEntity {
    public MFArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) { super(MFBlockEntities.ARCANE_WORKBENCH.get(), pos, state); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "primary", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.arcane_workbench.crystal_orbit"))));
    }
}
