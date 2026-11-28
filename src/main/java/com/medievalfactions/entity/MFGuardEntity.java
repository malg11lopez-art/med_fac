package com.medievalfactions.entity;

import com.medievalfactions.registry.MFSounds;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Map;

public final class MFGuardEntity extends MFAnimatedMob {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.guard.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.guard.walk");
    private static final RawAnimation SLASH = RawAnimation.begin().thenPlay("animation.guard.attack_light");
    private static final RawAnimation BLOCK = RawAnimation.begin().thenPlay("animation.guard.block");
    static final Map<String, String> ACTION_CLIPS = Map.ofEntries(
        Map.entry("heavy", "animation.guard.attack_heavy"),
        Map.entry("parry", "animation.guard.parry"),
        Map.entry("dodge", "animation.guard.dodge_roll"),
        Map.entry("shield_bash", "animation.guard.shield_bash"),
        Map.entry("bow_draw", "animation.guard.bow_draw"),
        Map.entry("bow_release", "animation.guard.bow_release"),
        Map.entry("crossbow_reload", "animation.guard.crossbow_reload"),
        Map.entry("staff_strike", "animation.guard.staff_strike"),
        Map.entry("greatsword_spin", "animation.guard.greatsword_spin"),
        Map.entry("spear_thrust", "animation.guard.spear_thrust"),
        Map.entry("dagger_combo", "animation.guard.dagger_combo"),
        Map.entry("axe_overhead", "animation.guard.axe_overhead"),
        Map.entry("hammer_slam", "animation.guard.hammer_slam"),
        Map.entry("shield_counter", "animation.guard.shield_counter"),
        Map.entry("jump_attack", "animation.guard.jump_attack"),
        Map.entry("heal_channel", "animation.guard.heal_channel"),
        Map.entry("summon_channel", "animation.guard.summon_channel"),
        Map.entry("bard_performance", "animation.guard.bard_performance"),
        Map.entry("monk_combo", "animation.guard.monk_combo"),
        Map.entry("berserker_roar", "animation.guard.berserker_roar"),
        Map.entry("cast", "animation.guard.cast"),
        Map.entry("cast_fire", "animation.guard.cast_fire"),
        Map.entry("cast_light", "animation.guard.cast_light"),
        Map.entry("cast_lightning", "animation.guard.cast_lightning"),
        Map.entry("cast_earth", "animation.guard.cast_earth"),
        Map.entry("cast_metal", "animation.guard.cast_metal"),
        Map.entry("cast_ether", "animation.guard.cast_ether"),
        Map.entry("cast_water", "animation.guard.cast_water"),
        Map.entry("cast_ice", "animation.guard.cast_ice"),
        Map.entry("cast_air", "animation.guard.cast_air"),
        Map.entry("cast_darkness", "animation.guard.cast_darkness"),
        Map.entry("cast_nature", "animation.guard.cast_nature"),
        Map.entry("cast_spirit", "animation.guard.cast_spirit"),
        Map.entry("mine", "animation.guard.mine"),
        Map.entry("chop_wood", "animation.guard.chop_wood"),
        Map.entry("saw", "animation.guard.saw"),
        Map.entry("fish_cast", "animation.guard.fish_cast"),
        Map.entry("write", "animation.guard.write"),
        Map.entry("alchemy", "animation.guard.alchemy"),
        Map.entry("cook", "animation.guard.cook"),
        Map.entry("build", "animation.guard.build"),
        Map.entry("carry", "animation.guard.carry"),
        Map.entry("harvest", "animation.guard.harvest")
    );
    public MFGuardEntity(EntityType<? extends MFGuardEntity> type, Level level) { super(type, level); }
    @Override protected String idleAnimation() { return "animation.guard.idle"; }
    @Override protected String walkAnimation() { return "animation.guard.walk"; }
    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.10D, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }
    @Override public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide) {
            playSlashAnimation();
            playSound(MFSounds.COMBAT_BLADE_HIT.get(), 0.85F, 0.92F + getRandom().nextFloat() * 0.16F);
        }
        return hit;
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        var actions = actionController()
            .triggerableAnim("slash", SLASH)
            .triggerableAnim("block", BLOCK);
        ACTION_CLIPS.forEach((trigger, clip) -> actions.triggerableAnim(trigger, RawAnimation.begin().thenPlay(clip)));
        controllers.add(actions);
    }
    /** Call on the logical server after validating the combat action. */
    public void playSlashAnimation() {
        if (!level().isClientSide) this.triggerAnim("action", "slash");
    }
    public void playBlockAnimation() {
        if (!level().isClientSide) this.triggerAnim("action", "block");
    }
    /** Plays a registered action clip after the caller has validated the gameplay action. */
    public boolean playAction(String action) {
        if (level().isClientSide || action == null || !ACTION_CLIPS.containsKey(action)) return false;
        this.triggerAnim("action", action);
        return true;
    }
}
