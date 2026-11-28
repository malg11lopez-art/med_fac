package com.medievalfactions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Visual/gameplay actor prototype. Species-specific needs, combat and flight logic are later milestones. */
public final class MFDraconidEntity extends MFAnimatedMob {
    public MFDraconidEntity(EntityType<? extends MFDraconidEntity> type, Level level) { super(type, level); }
    @Override protected String idleAnimation() { return "animation.draconid.idle"; }
    @Override protected String walkAnimation() { return "animation.draconid.ground_walk"; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("claw", RawAnimation.begin().thenPlay("animation.draconid.claw"))
            .triggerableAnim("wing_flap", RawAnimation.begin().thenLoop("animation.draconid.wing_flap")));
    }
    public void triggerPrimaryAction() { this.triggerAnim("action", "claw"); }
    public void triggerSecondaryAction() { this.triggerAnim("action", "wing_flap"); }
}
