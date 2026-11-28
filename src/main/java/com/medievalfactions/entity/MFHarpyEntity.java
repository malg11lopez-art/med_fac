package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Visual/gameplay actor prototype. Species-specific needs, combat and flight logic are later milestones. */
public final class MFHarpyEntity extends MFAnimatedMob {
    public MFHarpyEntity(EntityType<? extends MFHarpyEntity> type, Level level) { super(type, level); }
    @Override protected String idleAnimation() { return "animation.harpy.idle"; }
    @Override protected String walkAnimation() { return "animation.harpy.flap"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("takeoff", RawAnimation.begin().thenPlay("animation.harpy.takeoff"))
            .triggerableAnim("dive", RawAnimation.begin().thenPlay("animation.harpy.dive")));
    }
    public void triggerPrimaryAction() { this.triggerAnim("action", "takeoff"); }
    public void triggerSecondaryAction() { this.triggerAnim("action", "dive"); }
}
