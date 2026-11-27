package com.medievalfactions.entity;

import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.domain.MFCitizenSimulation;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFGameCatalog.Profession;
import com.medievalfactions.data.MFWorldSavedData;
import net.minecraft.core.BlockPos;
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
    private UUID citizenId;
    private BlockPos lastNavigationTarget;
    private String activeActionTrigger;

    public MFResidentEntity(EntityType<? extends MFResidentEntity> type, Level level) { super(type, level); }

    public void bindCitizen(UUID id) {
        if (id == null) throw new IllegalArgumentException("Citizen identity required");
        this.citizenId = id;
        setPersistenceRequired();
    }
    public UUID citizenId() { return citizenId; }

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
                    other -> other != this && other.citizenId != null);
            MFResidentEntity neighbour = neighbours.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (neighbour != null) getLookControl().setLookAt(neighbour, 10.0F, 40.0F);
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
            // Work animations run only after a real crop harvest or accepted station job.
            setActionTrigger(plan.activity() == MFCitizenSimulation.Activity.WORK && !workActionCompleted
                    ? null : actionFor(plan.activity(), citizen.profession()));
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
                || citizen.profession() != Profession.FARMER) return false;
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
        if (!worldData.addSettlementStock(citizen.settlementId(), itemId, amount)) {
            level.setBlock(position, original, 3);
            return false;
        }
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
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        citizenId = tag.hasUUID("mf_citizen_id") ? tag.getUUID("mf_citizen_id") : null;
        lastNavigationTarget = null;
        activeActionTrigger = null;
    }
}
