package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Persistent griffin mount with controlled flight and animated swoop/dive attacks. */
public final class MFGriffinEntity extends MFAnimatedMob {
    public MFGriffinEntity(EntityType<? extends MFGriffinEntity> type, Level level) { super(type, level); }
    @Override protected boolean isRideable() { return true; }
    @Override protected boolean isFlyingMount() { return true; }
    @Override protected String idleAnimation() { return "animation.griffin.idle"; }
    @Override protected String walkAnimation() { return isNoGravity() ? "animation.griffin.fly" : "animation.griffin.idle"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("swoop", RawAnimation.begin().thenPlay("animation.griffin.swoop"))
            .triggerableAnim("dive", RawAnimation.begin().thenPlay("animation.griffin.dive")));
    }
    public void triggerPrimaryAction() { if (!level().isClientSide) this.triggerAnim("action", "swoop"); }
    public void triggerSecondaryAction() { if (!level().isClientSide) this.triggerAnim("action", "dive"); }
}
