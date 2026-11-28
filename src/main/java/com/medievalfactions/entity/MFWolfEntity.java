package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Persistent giant-wolf mount with ground controls, bond progression and an attack action. */
public final class MFWolfEntity extends MFAnimatedMob {
    public MFWolfEntity(EntityType<? extends MFWolfEntity> type, Level level) { super(type, level); }
    @Override protected boolean isRideable() { return true; }
    @Override protected String idleAnimation() { return "animation.wolf.idle"; }
    @Override protected String walkAnimation() { return "animation.wolf.walk"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("run", RawAnimation.begin().thenLoop("animation.wolf.run"))
            .triggerableAnim("pounce", RawAnimation.begin().thenPlay("animation.wolf.pounce"))
            .triggerableAnim("howl", RawAnimation.begin().thenPlay("animation.wolf.howl")));
    }
    public void triggerPrimaryAction() { if (!level().isClientSide) this.triggerAnim("action", "run"); }
    public void triggerSecondaryAction() { if (!level().isClientSide) this.triggerAnim("action", "pounce"); }
    public void triggerHowl() { if (!level().isClientSide) this.triggerAnim("action", "howl"); }
}
