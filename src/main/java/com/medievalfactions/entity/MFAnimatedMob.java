package com.medievalfactions.entity;

import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.domain.MFAdvancedSystems.Companion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;

/** Common prototype base for animated faction actors. Detailed faction AI is added in later milestones. */
public abstract class MFAnimatedMob extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID mountOwner;

    protected MFAnimatedMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    protected abstract String idleAnimation();
    protected abstract String walkAnimation();

    /** Only designated species opt into player-controlled riding. */
    protected boolean isRideable() { return false; }
    /** Flying species use direct, server-authoritative three-dimensional movement while ridden. */
    protected boolean isFlyingMount() { return false; }

    public void setMountOwner(UUID owner) {
        this.mountOwner = owner;
        if (owner != null) setPersistenceRequired();
    }
    public UUID getMountOwner() { return mountOwner; }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        if (!isRideable() || !(passenger instanceof Player player) || !getPassengers().isEmpty()
                || (mountOwner != null && !mountOwner.equals(player.getUUID()))) return false;
        // On the logical server, ownership, health, stamina and hunger are checked against the persistent ledger.
        if (mountOwner != null && level() instanceof ServerLevel serverLevel) {
            return MFAdvancedSavedData.get(serverLevel.getServer()).record("companions", getUUID(), Companion.class)
                    .map(companion -> companion.canRide(player.getUUID())).orElse(false);
        }
        return true;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        if (passenger instanceof LivingEntity living && (mountOwner == null || mountOwner.equals(living.getUUID()))) return living;
        return null;
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity controller = getControllingPassenger();
        if (isRideable() && controller instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(isFlyingMount() ? player.getXRot() * 0.25F : 0.0F);
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float strafe = player.xxa * 0.5F;
            float forward = player.zza;
            if (forward <= 0.0F) forward *= 0.25F;
            if (isFlyingMount()) {
                this.setNoGravity(true);
                double yaw = Math.toRadians(player.getYRot());
                double pitch = Math.toRadians(player.getXRot());
                double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.65D;
                double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
                double rightX = Math.cos(yaw), rightZ = Math.sin(yaw);
                double moveX = forwardX * forward + rightX * strafe;
                double moveZ = forwardZ * forward + rightZ * strafe;
                double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
                if (length > 1.0D) { moveX /= length; moveZ /= length; }
                double moveY = -Math.sin(pitch) * forward * speed;
                if (player.isShiftKeyDown()) moveY = -speed * 0.55D;
                else if (Math.abs(forward) < 0.05D) moveY *= 0.25D;
                this.setDeltaMovement(moveX * speed, moveY, moveZ * speed);
                this.move(MoverType.SELF, this.getDeltaMovement());
                return;
            }
            this.setNoGravity(false);
            super.travel(new Vec3(strafe, travelVector.y, forward));
            return;
        }
        this.setNoGravity(false);
        super.travel(travelVector);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (mountOwner != null) tag.putUUID("mf_mount_owner", mountOwner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mountOwner = tag.hasUUID("mf_mount_owner") ? tag.getUUID("mf_mount_owner") : null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    protected void addLocomotionController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 5, state ->
            state.setAndContinue(RawAnimation.begin().thenLoop(state.isMoving() ? walkAnimation() : idleAnimation()))
        ));
    }

    protected AnimationController<MFAnimatedMob> actionController() {
        return new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
            .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
