package com.medievalfactions.entity;

import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.domain.MFCitizenSimulation;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFGameCatalog.Profession;
import com.medievalfactions.data.MFWorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.UUID;

/** Visible resident linked to a persistent citizen record; server plans drive vanilla path navigation. */
public final class MFResidentEntity extends MFAnimatedMob {
    private static final EntityDataAccessor<Integer> VISUAL_ROLE =
            SynchedEntityData.defineId(MFResidentEntity.class, EntityDataSerializers.INT);
    private UUID citizenId;
    private BlockPos lastNavigationTarget;
    private String activeActionTrigger;
    private long lastSocialInteractionTick = -1L;
    private long lastMealTick = -1L;

    public MFResidentEntity(EntityType<? extends MFResidentEntity> type, Level level) { super(type, level); }

    public void bindCitizen(UUID id) {
        if (id == null) throw new IllegalArgumentException("Citizen identity required");
        this.citizenId = id;
        if (level() instanceof ServerLevel serverLevel) {
            MFWorldSavedData.get(serverLevel.getServer()).citizen(id)
                    .ifPresent(citizen -> setVisualRole(visualRoleFor(citizen.profession())));
        }
        setPersistenceRequired();
    }
    public UUID citizenId() { return citizenId; }

    /** Synchronized presentation key; authoritative profession remains in SavedData. */
    public int visualRole() { return getEntityData().get(VISUAL_ROLE); }

    private void setVisualRole(int role) {
        int safeRole = role >= 0 && role <= 6 ? role : 0;
        if (getEntityData().get(VISUAL_ROLE) != safeRole) getEntityData().set(VISUAL_ROLE, safeRole);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VISUAL_ROLE, 0);
    }

    private static int visualRoleFor(Profession profession) {
        if (profession == null) return 0;
        return switch (profession) {
            case FARMER, RANCHER, SHEPHERD, HORSE_BREEDER, APICULTURIST -> 1;
            case BLACKSMITH, ARMORER, SMELTER, MASON, ENGINEER -> 2;
            case LIBRARIAN, SCRIBE, TEACHER, RESEARCHER, CARTOGRAPHER, ARCANE_RESEARCHER,
                    ELEMENTAL_MASTER, MAGICAL_ARTISAN -> 3;
            case DOCTOR, SURGEON, VETERINARIAN, PRIEST -> 4;
            case TRADER, APPRAISER, WHOLESALER, BANKER, TREASURER, MINT_MASTER,
                    ACCOUNTANT, TAX_COLLECTOR, LENDER, AUCTIONEER, TRANSPORTER,
                    CARAVANER, MESSENGER -> 5;
            case GUARD, CAPTAIN -> 6;
            default -> 0;
        };
    }

    @Override protected String idleAnimation() { return "animation.guard.idle"; }
    @Override protected String walkAnimation() { return "animation.guard.walk"; }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        addLocomotionController(controllers);
        var actions = actionController()
            .triggerableAnim("resident_sleep", RawAnimation.begin().thenLoop("animation.guard.resident_sleep"))
            .triggerableAnim("resident_eat", RawAnimation.begin().thenLoop("animation.guard.resident_eat"))
            .triggerableAnim("resident_socialize", RawAnimation.begin().thenLoop("animation.guard.resident_socialize"))
            .triggerableAnim("resident_train", RawAnimation.begin().thenLoop("animation.guard.resident_train"));
        MFGuardEntity.ACTION_CLIPS.forEach((trigger, clip) -> actions.triggerableAnim(trigger, RawAnimation.begin().thenPlay(clip)));
        controllers.add(actions);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide || citizenId == null || tickCount % 10 != 0 || !(level() instanceof ServerLevel serverLevel)) return;
        var data = MFAdvancedSavedData.get(serverLevel.getServer());
        MFWorldSavedData worldData = MFWorldSavedData.get(serverLevel.getServer());
        MFCitizenState citizen = worldData.citizen(citizenId).orElse(null);
        var plan = data.record("citizen_plans", citizenId, MFCitizenSimulation.CitizenPlan.class).orElse(null);
        if (plan == null || citizen == null || citizen.status() != com.medievalfactions.domain.MFGameCatalog.CitizenStatus.ACTIVE) {
            getNavigation().stop(); lastNavigationTarget = null; setActionTrigger(null); return;
        }
        if (plan.activity() == MFCitizenSimulation.Activity.SOCIALIZE) {
            var neighbours = serverLevel.getEntitiesOfClass(MFResidentEntity.class, getBoundingBox().inflate(8.0D),
                    other -> other != this && other.citizenId != null
                            && worldData.citizen(other.citizenId)
                            .map(otherCitizen -> otherCitizen.status()
                                    == com.medievalfactions.domain.MFGameCatalog.CitizenStatus.ACTIVE
                                    && otherCitizen.settlementId().equals(citizen.settlementId()))
                            .orElse(false));
            MFResidentEntity neighbour = neighbours.stream()
                    .min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (neighbour != null) {
                getLookControl().setLookAt(neighbour, 10.0F, 40.0F);
                long now = serverLevel.getGameTime();
                boolean cooldownElapsed = lastSocialInteractionTick < 0L
                        || now < lastSocialInteractionTick || now - lastSocialInteractionTick >= 600L;
                // Only the lower UUID initiates the pair interaction, avoiding duplicate rewards
                // when both residents process their SOCIALIZE schedule in the same server tick.
                if (citizenId.compareTo(neighbour.citizenId) < 0 && cooldownElapsed
                        && worldData.socializeCitizens(citizenId, neighbour.citizenId, 1)) {
                    lastSocialInteractionTick = now;
                    serverLevel.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.VILLAGER_YES,
                            net.minecraft.sounds.SoundSource.NEUTRAL, 0.42F,
                            0.96F + getRandom().nextFloat() * 0.08F);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            getX(), getY() + 1.0D, getZ(), 2, 0.22D, 0.18D, 0.22D, 0.01D);
                }
            }
        }
        MFCitizenSimulation.Waypoint destination = plan.destination();
        if (!serverLevel.dimension().location().toString().equals(destination.dimension())) {
            getNavigation().stop(); lastNavigationTarget = null; return;
        }
        BlockPos target = new BlockPos(destination.x(), destination.y(), destination.z());
        if (distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D) <= 2.25D) {
            getNavigation().stop();
            lastNavigationTarget = target;
            // Work orders begin only at a compatible, physically present station.
            // Waypoints may identify the workstation block itself or the standing
            // position one block above it.
            boolean workActionCompleted = false;
            if (plan.activity() == MFCitizenSimulation.Activity.WORK) {
                if (citizen.profession() == Profession.FARMER) {
                    workActionCompleted = harvestMatureCrop(serverLevel, worldData, citizen, target)
                            || harvestMatureCrop(serverLevel, worldData, citizen, target.below());
                } else {
                    String stationAtTarget = BuiltInRegistries.BLOCK.getKey(
                            serverLevel.getBlockState(target).getBlock()).toString();
                    var started = data.startResidentWorkOrder(
                            worldData, citizenId, stationAtTarget, worldData.simulatedDays());
                    if (started.isEmpty()) {
                        String stationBelow = BuiltInRegistries.BLOCK.getKey(
                                serverLevel.getBlockState(target.below()).getBlock()).toString();
                        started = data.startResidentWorkOrder(
                                worldData, citizenId, stationBelow, worldData.simulatedDays());
                    }
                    workActionCompleted = started.isPresent();
                }
            }
            boolean eatActionCompleted = false;
            if (plan.activity() == MFCitizenSimulation.Activity.EAT) {
                long now = serverLevel.getGameTime();
                boolean mealCooldownElapsed = lastMealTick < 0L || now < lastMealTick || now - lastMealTick >= 600L;
                if (mealCooldownElapsed && worldData.feedResident(citizen.settlementId(), citizenId)) {
                    lastMealTick = now;
                    eatActionCompleted = true;
                    serverLevel.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EAT,
                            net.minecraft.sounds.SoundSource.NEUTRAL, 0.48F,
                            0.94F + getRandom().nextFloat() * 0.12F);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            getX(), getY() + 1.0D, getZ(), 2, 0.18D, 0.16D, 0.18D, 0.01D);
                } else {
                    eatActionCompleted = lastMealTick >= 0L && now >= lastMealTick && now - lastMealTick < 40L;
                }
            }
            // Work/eating animations run only after a real crop harvest, accepted station job or consumed meal.
            boolean actionSucceeded = (plan.activity() != MFCitizenSimulation.Activity.WORK || workActionCompleted)
                    && (plan.activity() != MFCitizenSimulation.Activity.EAT || eatActionCompleted);
            setActionTrigger(actionSucceeded ? actionFor(plan.activity(), citizen.profession()) : null);
            data.reportResidentWaypointArrival(serverLevel, citizenId);
            return;
        }
        // Locomotion owns the animation while the resident is walking to the destination.
        setActionTrigger(null);
        if (!target.equals(lastNavigationTarget) || getNavigation().isDone()) {
            double speed = plan.directive() == MFCitizenSimulation.Directive.FLEE ? 1.25D
                    : plan.directive() == MFCitizenSimulation.Directive.SEEK_MEDIC ? 1.05D : 0.9D;
            getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, speed);
            lastNavigationTarget = target;
        }
    }

    private static boolean harvestMatureCrop(ServerLevel level, MFWorldSavedData worldData,
                                              MFCitizenState citizen, BlockPos position) {
        if (level == null || worldData == null || citizen == null || position == null
                || citizen.profession() != Profession.FARMER || !level.hasChunkAt(position)) return false;
        var original = level.getBlockState(position);
        if (!(original.getBlock() instanceof CropBlock) || !original.hasProperty(CropBlock.AGE)) return false;

        String itemId;
        long amount;
        int matureAge;
        if (original.is(Blocks.WHEAT)) {
            itemId = BuiltInRegistries.ITEM.getKey(Items.WHEAT).toString();
            amount = 1L;
            matureAge = 7;
        } else if (original.is(Blocks.CARROTS)) {
            itemId = BuiltInRegistries.ITEM.getKey(Items.CARROT).toString();
            amount = 2L;
            matureAge = 7;
        } else if (original.is(Blocks.POTATOES)) {
            itemId = BuiltInRegistries.ITEM.getKey(Items.POTATO).toString();
            amount = 2L;
            matureAge = 7;
        } else if (original.is(Blocks.BEETROOTS)) {
            itemId = BuiltInRegistries.ITEM.getKey(Items.BEETROOT).toString();
            amount = 1L;
            matureAge = 3;
        } else {
            return false;
        }

        if (original.getValue(CropBlock.AGE) < matureAge) return false;
        var replanted = original.setValue(CropBlock.AGE, 0);
        if (!level.setBlock(position, replanted, 3)) return false;
        if (!worldData.creditResidentHarvest(citizen.settlementId(), citizen.id(), itemId, amount, 12L)) {
            level.setBlock(position, original, 3);
            return false;
        }
        // Keep harvest feedback separate from block replacement: play the crop's vanilla break sound
        // and emit a small success burst without invoking the client-side block-break level event.
        level.playSound(null, position, net.minecraft.sounds.SoundEvents.CROP_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.72F, 0.96F + level.random.nextFloat() * 0.08F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                position.getX() + 0.5D, position.getY() + 0.65D, position.getZ() + 0.5D,
                3, 0.18D, 0.12D, 0.18D, 0.01D);
        return true;
    }

    private void setActionTrigger(String nextTrigger) {
        if (java.util.Objects.equals(activeActionTrigger, nextTrigger)) return;
        if (activeActionTrigger != null) stopTriggeredAnim("action", activeActionTrigger);
        activeActionTrigger = nextTrigger;
        if (nextTrigger != null) triggerAnim("action", nextTrigger);
    }

    private static String actionFor(MFCitizenSimulation.Activity activity, Profession profession) {
        if (activity == null) return null;
        if (activity != MFCitizenSimulation.Activity.WORK) {
            return switch (activity) {
                case SLEEP, REST -> "resident_sleep";
                case EAT -> "resident_eat";
                case SOCIALIZE -> "resident_socialize";
                case TRAIN -> "resident_train";
                case GUARD -> "block";
                case VISIT_MEDIC -> "heal_channel";
                case TRAVEL -> null;
                case WORK -> throw new IllegalStateException("Handled above");
            };
        }
        if (profession == null) return "craft";
        return switch (profession) {
            case MINER, STONECUTTER, PROSPECTOR -> "mine";
            case LUMBERJACK -> "chop_wood";
            case CARPENTER, SHIPWRIGHT, CARTWRIGHT -> "saw";
            case BLACKSMITH, ARMORER, SMELTER -> "hammer_slam";
            case RANGED_ARTISAN, LEATHERWORKER, TAILOR, JEWELER -> "craft";
            case MAGICAL_ARTISAN, ALCHEMIST, ARCANE_RESEARCHER, ELEMENTAL_MASTER -> "alchemy";
            case MASON, ENGINEER, BUILDER, ARCHITECT -> "build";
            case BAKER, BUTCHER, MILLER -> "cook";
            case FARMER, RANCHER, SHEPHERD, HORSE_BREEDER, APICULTURIST -> "harvest";
            case FISHER -> "fish_cast";
            case LIBRARIAN, SCRIBE, TEACHER, RESEARCHER, CARTOGRAPHER, DIPLOMAT, JUDGE -> "write";
            case DOCTOR, SURGEON, VETERINARIAN, PRIEST -> "heal_channel";
            case GUARD, CAPTAIN -> "block";
            case TRADER, APPRAISER, WHOLESALER, BANKER, TREASURER, MINT_MASTER, ACCOUNTANT,
                    TAX_COLLECTOR, LENDER, AUCTIONEER, TRANSPORTER, CARAVANER, MESSENGER,
                    SETTLEMENT_ADMINISTRATOR -> "carry";
        };
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (citizenId != null) tag.putUUID("mf_citizen_id", citizenId);
        tag.putInt("mf_visual_role", visualRole());
        tag.putLong("mf_last_social_tick", lastSocialInteractionTick);
        tag.putLong("mf_last_meal_tick", lastMealTick);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        citizenId = tag.hasUUID("mf_citizen_id") ? tag.getUUID("mf_citizen_id") : null;
        int savedRole = tag.contains("mf_visual_role", net.minecraft.nbt.Tag.TAG_INT)
                ? tag.getInt("mf_visual_role") : 0;
        setVisualRole(savedRole);
        if (savedRole == 0 && citizenId != null && level() instanceof ServerLevel serverLevel) {
            MFWorldSavedData.get(serverLevel.getServer()).citizen(citizenId)
                    .ifPresent(citizen -> setVisualRole(visualRoleFor(citizen.profession())));
        }
        lastNavigationTarget = null;
        activeActionTrigger = null;
        lastSocialInteractionTick = tag.contains("mf_last_social_tick", net.minecraft.nbt.Tag.TAG_LONG)
                ? tag.getLong("mf_last_social_tick") : -1L;
        lastMealTick = tag.contains("mf_last_meal_tick", net.minecraft.nbt.Tag.TAG_LONG)
                ? tag.getLong("mf_last_meal_tick") : -1L;
    }
}
