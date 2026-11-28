package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Persistent phoenix mount with controlled flight and fire-oriented dive attacks. */
public final class MFPhoenixEntity extends MFAnimatedMob {
    public MFPhoenixEntity(EntityType<? extends MFPhoenixEntity> type, Level level) { super(type, level); }
    @Override protected boolean isRideable() { return true; }
    @Override protected boolean isFlyingMount() { return true; }
    @Override protected String idleAnimation() { return "animation.phoenix.idle"; }
    @Override protected String walkAnimation() { return isNoGravity() ? "animation.phoenix.hover" : "animation.phoenix.idle"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("dive", RawAnimation.begin().thenPlay("animation.phoenix.dive"))
            .triggerableAnim("rebirth", RawAnimation.begin().thenPlay("animation.phoenix.rebirth")));
    }
    public void triggerPrimaryAction() { if (!level().isClientSide) this.triggerAnim("action", "dive"); }
    public void triggerSecondaryAction() { if (!level().isClientSide) this.triggerAnim("action", "rebirth"); }
}
