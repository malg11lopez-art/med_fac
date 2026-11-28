package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Visual/gameplay actor prototype. Species-specific needs, combat and flight logic are later milestones. */
public final class MFMerfolkEntity extends MFAnimatedMob {
    public MFMerfolkEntity(EntityType<? extends MFMerfolkEntity> type, Level level) { super(type, level); }
    @Override protected String idleAnimation() { return "animation.merfolk.idle"; }
    @Override protected String walkAnimation() { return "animation.merfolk.swim"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("swim_fast", RawAnimation.begin().thenLoop("animation.merfolk.swim_fast"))
            .triggerableAnim("strike", RawAnimation.begin().thenPlay("animation.merfolk.strike")));
    }
    public void triggerPrimaryAction() { this.triggerAnim("action", "swim_fast"); }
    public void triggerSecondaryAction() { this.triggerAnim("action", "strike"); }
}
