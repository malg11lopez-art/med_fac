package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Visual/gameplay actor prototype. Species-specific needs, combat and flight logic are later milestones. */
public final class MFCentaurEntity extends MFAnimatedMob {
    public MFCentaurEntity(EntityType<? extends MFCentaurEntity> type, Level level) { super(type, level); }
    @Override protected String idleAnimation() { return "animation.centaur.idle"; }
    @Override protected String walkAnimation() { return "animation.centaur.walk"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("gallop", RawAnimation.begin().thenPlay("animation.centaur.gallop"))
            .triggerableAnim("spear_thrust", RawAnimation.begin().thenPlay("animation.centaur.spear_thrust")));
    }
    public void triggerPrimaryAction() { this.triggerAnim("action", "gallop"); }
    public void triggerSecondaryAction() { this.triggerAnim("action", "spear_thrust"); }
}
