package com.medievalfactions.entity;

import com.medievalfactions.registry.MFSounds;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** Persistent, owner-bound dragon mount with controlled flight and a server-validated fire breath action. */
public final class MFDragonEntity extends MFAnimatedMob {
    private int breathWindupTicks;
    private long nextBreathGameTime;
    private static final int BREATH_WINDUP_TICKS = 12;
    private static final int BREATH_COOLDOWN_TICKS = 80;
    private static final double BREATH_RANGE = 15.0D;
    private static final double BREATH_CONE_DOT = 0.78D;

    public MFDragonEntity(EntityType<? extends MFDragonEntity> type, Level level) { super(type, level); }
    @Override protected boolean isRideable() { return true; }
    @Override protected boolean isFlyingMount() { return true; }
    @Override protected String idleAnimation() { return isNoGravity() ? "animation.dragon.glide" : "animation.dragon.idle"; }
    @Override protected String walkAnimation() { return isNoGravity() ? "animation.dragon.fly" : "animation.dragon.ground_walk"; }

    @Override public void travel(Vec3 travelVector) {
        boolean wasFlying = isNoGravity();
        super.travel(travelVector);
        boolean isFlying = isNoGravity();
        if (!level().isClientSide && !wasFlying && isFlying) triggerAnim("action", "takeoff");
        else if (!level().isClientSide && wasFlying && !isFlying) triggerAnim("action", "land");
    }
    /**
     * Autonomous dragons telegraph a fire-breath attack before applying a server-side cone hit.
     * A mounted dragon never fires autonomously; the owner controls it.
     */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved() || getControllingPassenger() != null) return;
        if (tickCount % 5 != 0) return;
        LivingEntity target = getTarget();
        long now = level().getGameTime();
        if (target == null || !target.isAlive() || target.level() != level()
                || distanceToSqr(target) > BREATH_RANGE * BREATH_RANGE || !hasLineOfSight(target)) {
            breathWindupTicks = 0;
            return;
        }
        if (breathWindupTicks == 0) {
            if (now < nextBreathGameTime) return;
            breathWindupTicks = BREATH_WINDUP_TICKS;
            triggerAnim("action", "roar");
            playSound(MFSounds.CREATURE_DRAGON_BREATH_CHARGE.get(), 1.0F, 0.95F);
            return;
        }
        breathWindupTicks = Math.max(0, breathWindupTicks - 5);
        if (breathWindupTicks == 0) {
            releaseBreath(target);
            nextBreathGameTime = now + BREATH_COOLDOWN_TICKS;
        }
    }

    private void releaseBreath(LivingEntity primaryTarget) {
        if (!(level() instanceof ServerLevel serverLevel) || primaryTarget == null || !primaryTarget.isAlive()) return;
        playBreathAnimation();
        playSound(MFSounds.CREATURE_DRAGON_BREATH_RELEASE.get(), 1.1F, 0.9F + getRandom().nextFloat() * 0.15F);
        Vec3 origin = getEyePosition();
        Vec3 direction = primaryTarget.getEyePosition().subtract(origin).normalize();
        var area = getBoundingBox().expandTowards(direction.scale(BREATH_RANGE)).inflate(2.0D, 1.5D, 2.0D);
        int hits = 0;
        for (Monster victim : level().getEntitiesOfClass(Monster.class, area,
                candidate -> candidate != this && candidate.isAlive())) {
            Vec3 toVictim = victim.getEyePosition().subtract(origin);
            double distance = toVictim.length();
            if (distance < 1.0D || distance > BREATH_RANGE || direction.dot(toVictim.normalize()) < BREATH_CONE_DOT
                    || !hasLineOfSight(victim)) continue;
            float damage = (float) Math.max(4.0D, 12.0D - distance * 0.35D);
            if (victim.hurt(damageSources().mobAttack(this), damage)) {
                victim.setSecondsOnFire(4);
                serverLevel.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY() + 0.7D, victim.getZ(),
                        10, 0.35D, 0.45D, 0.35D, 0.02D);
                hits++;
            }
            if (hits >= 8) break;
        }
        serverLevel.sendParticles(ParticleTypes.FLAME, origin.x + direction.x * 2.0D,
                origin.y + direction.y * 2.0D, origin.z + direction.z * 2.0D,
                28, 0.7D, 0.6D, 0.7D, 0.12D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("mf_dragon_next_breath", nextBreathGameTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        nextBreathGameTime = Math.max(0L, tag.getLong("mf_dragon_next_breath"));
        breathWindupTicks = 0;
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true) {
            @Override public boolean canUse() {
                return getControllingPassenger() == null && super.canUse();
            }
            @Override public boolean canContinueToUse() {
                return getControllingPassenger() == null && super.canContinueToUse();
            }
        });
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }
    @Override public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide) {
            triggerAnim("action", "bite");
            playSound(MFSounds.CREATURE_DRAGON_BITE.get(), 1.0F, 0.92F + getRandom().nextFloat() * 0.16F);
        }
        return hit;
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        controllers.add(actionController()
            .triggerableAnim("roar", RawAnimation.begin().thenPlay("animation.dragon.roar"))
            .triggerableAnim("bite", RawAnimation.begin().thenPlay("animation.dragon.bite"))
            .triggerableAnim("breath", RawAnimation.begin().thenPlay("animation.dragon.breath_fire"))
            .triggerableAnim("takeoff", RawAnimation.begin().thenPlay("animation.dragon.takeoff"))
            .triggerableAnim("land", RawAnimation.begin().thenPlay("animation.dragon.land")));
    }
    /** Call on the logical server only after the dragon AI validates the action. */
    public void playRoar() { if (!level().isClientSide) this.triggerAnim("action", "roar"); }
    public void playBreathAnimation() { if (!level().isClientSide) this.triggerAnim("action", "breath"); }
}
