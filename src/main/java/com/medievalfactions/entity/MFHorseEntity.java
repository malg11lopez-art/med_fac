package com.medievalfactions.entity;

import com.medievalfactions.registry.MFSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationController;

/** Persistent owner-bound horse mount with ground riding controls and animated mount actions. */
public final class MFHorseEntity extends MFAnimatedMob {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.horse.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.horse.walk");
    public MFHorseEntity(EntityType<? extends MFHorseEntity> type, Level level) { super(type, level); }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive() && onGround() && !isPassenger()
                && getDeltaMovement().horizontalDistanceSqr() > 0.0064D && tickCount % 8 == 0) {
            playSound(MFSounds.CREATURE_HORSE_STEP.get(), 0.38F, 0.92F + getRandom().nextFloat() * 0.16F);
        }
    }
    @Override protected boolean isRideable() { return true; }
    @Override protected String idleAnimation() { return "animation.horse.idle"; }
    @Override protected String walkAnimation() {
        return getDeltaMovement().horizontalDistanceSqr() > 0.055D
                ? "animation.horse.gallop" : "animation.horse.walk";
    }

    @Override public void jumpFromGround() {
        super.jumpFromGround();
        if (!level().isClientSide) triggerAnim("mount_action", "jump");
    }

    public void triggerRearAnimation() {
        if (!level().isClientSide) triggerAnim("mount_action", "rear");
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(new AnimationController<>(this, "mount_action", 0, state -> PlayState.STOP)
            .triggerableAnim("rear", RawAnimation.begin().thenPlay("animation.horse.rear"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.horse.jump"))
            .setSoundKeyframeHandler(new software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler<>()));
    }
}
