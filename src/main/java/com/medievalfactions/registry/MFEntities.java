package com.medievalfactions.registry;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MFEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MedievalFactions.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<MFGuardEntity>> GUARD = ENTITY_TYPES.register("guard", () -> EntityType.Builder.of(MFGuardEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F).clientTrackingRange(8).build("guard"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.medievalfactions.entity.MFResidentEntity>> RESIDENT = ENTITY_TYPES.register("resident", () -> EntityType.Builder.of(com.medievalfactions.entity.MFResidentEntity::new, MobCategory.CREATURE).sized(0.6F, 1.8F).clientTrackingRange(8).build("resident"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFHorseEntity>> HORSE = ENTITY_TYPES.register("horse_mount", () -> EntityType.Builder.of(MFHorseEntity::new, MobCategory.CREATURE).sized(1.4F, 1.6F).clientTrackingRange(10).build("horse_mount"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFDragonEntity>> DRAGON = ENTITY_TYPES.register("dragon", () -> EntityType.Builder.of(MFDragonEntity::new, MobCategory.CREATURE).sized(2.8F, 2.6F).clientTrackingRange(12).build("dragon"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFCentaurEntity>> CENTAUR = ENTITY_TYPES.register("centaur", () -> EntityType.Builder.of(MFCentaurEntity::new, MobCategory.CREATURE).sized(1.2F, 2.4F).clientTrackingRange(8).build("centaur"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFHarpyEntity>> HARPY = ENTITY_TYPES.register("harpy", () -> EntityType.Builder.of(MFHarpyEntity::new, MobCategory.CREATURE).sized(0.7F, 1.9F).clientTrackingRange(8).build("harpy"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFMerfolkEntity>> MERFOLK = ENTITY_TYPES.register("merfolk", () -> EntityType.Builder.of(MFMerfolkEntity::new, MobCategory.CREATURE).sized(0.7F, 1.8F).clientTrackingRange(8).build("merfolk"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFDraconidEntity>> DRACONID = ENTITY_TYPES.register("draconid", () -> EntityType.Builder.of(MFDraconidEntity::new, MobCategory.CREATURE).sized(0.8F, 2.1F).clientTrackingRange(8).build("draconid"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFWolfEntity>> WOLF = ENTITY_TYPES.register("giant_wolf", () -> EntityType.Builder.of(MFWolfEntity::new, MobCategory.CREATURE).sized(0.9F, 0.9F).clientTrackingRange(8).build("giant_wolf"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFGriffinEntity>> GRIFFIN = ENTITY_TYPES.register("griffin", () -> EntityType.Builder.of(MFGriffinEntity::new, MobCategory.CREATURE).sized(1.5F, 1.7F).clientTrackingRange(10).build("griffin"));
    public static final DeferredHolder<EntityType<?>, EntityType<MFPhoenixEntity>> PHOENIX = ENTITY_TYPES.register("phoenix", () -> EntityType.Builder.of(MFPhoenixEntity::new, MobCategory.CREATURE).sized(0.9F, 1.9F).clientTrackingRange(10).build("phoenix"));
    private MFEntities() {}
}
