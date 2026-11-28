package com.medievalfactions.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Generated SoundEvent registry for MEDIEVAL FACTIONS. */
public final class MFSounds {
    public static final String MOD_ID = "medieval_factions";
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);

    private MFSounds() {}

    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_CAMPFIRE_LOOP = SOUND_EVENTS.register("ambient_campfire_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_campfire_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_DUNGEON_LOOP = SOUND_EVENTS.register("ambient_dungeon_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_dungeon_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_FOREST_LOOP = SOUND_EVENTS.register("ambient_forest_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_forest_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_FORGE_LOOP = SOUND_EVENTS.register("ambient_forge_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_forge_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_TAVERN_LOOP = SOUND_EVENTS.register("ambient_tavern_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_tavern_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_TOWN_DAY_LOOP = SOUND_EVENTS.register("ambient_town_day_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ambient_town_day_loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_ARMOR_HIT = SOUND_EVENTS.register("combat_armor_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_armor_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_ARROW_RELEASE = SOUND_EVENTS.register("combat_arrow_release",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_arrow_release")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_BLADE_HIT = SOUND_EVENTS.register("combat_blade_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_blade_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_BOW_DRAW = SOUND_EVENTS.register("combat_bow_draw",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_bow_draw")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_CROSSBOW_LOAD = SOUND_EVENTS.register("combat_crossbow_load",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_crossbow_load")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_FOOTSTEP_GRASS = SOUND_EVENTS.register("combat_footstep_grass",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_footstep_grass")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_FOOTSTEP_SAND = SOUND_EVENTS.register("combat_footstep_sand",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_footstep_sand")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_FOOTSTEP_SNOW = SOUND_EVENTS.register("combat_footstep_snow",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_footstep_snow")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_FOOTSTEP_STONE = SOUND_EVENTS.register("combat_footstep_stone",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_footstep_stone")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_FOOTSTEP_WOOD = SOUND_EVENTS.register("combat_footstep_wood",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_footstep_wood")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_JUMP = SOUND_EVENTS.register("combat_jump",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_jump")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_LAND = SOUND_EVENTS.register("combat_land",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_land")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_PARRY = SOUND_EVENTS.register("combat_parry",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_parry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_SHIELD_BLOCK = SOUND_EVENTS.register("combat_shield_block",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_shield_block")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_SWORD_DRAW = SOUND_EVENTS.register("combat_sword_draw",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_sword_draw")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_SWORD_SWING_HEAVY = SOUND_EVENTS.register("combat_sword_swing_heavy",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_sword_swing_heavy")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_SWORD_SWING_LIGHT = SOUND_EVENTS.register("combat_sword_swing_light",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "combat_sword_swing_light")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_DRAGON_BITE = SOUND_EVENTS.register("creature_dragon_bite",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_dragon_bite")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_DRAGON_BREATH_CHARGE = SOUND_EVENTS.register("creature_dragon_breath_charge",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_dragon_breath_charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_DRAGON_BREATH_RELEASE = SOUND_EVENTS.register("creature_dragon_breath_release",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_dragon_breath_release")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_DRAGON_ROAR = SOUND_EVENTS.register("creature_dragon_roar",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_dragon_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_DRAGON_WING_FLAP = SOUND_EVENTS.register("creature_dragon_wing_flap",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_dragon_wing_flap")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_GRIFFIN_SCREECH = SOUND_EVENTS.register("creature_griffin_screech",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_griffin_screech")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_HORSE_NEIGH = SOUND_EVENTS.register("creature_horse_neigh",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_horse_neigh")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_HORSE_STEP = SOUND_EVENTS.register("creature_horse_step",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_horse_step")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_PHOENIX_CRY = SOUND_EVENTS.register("creature_phoenix_cry",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_phoenix_cry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_PHOENIX_FLARE = SOUND_EVENTS.register("creature_phoenix_flare",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_phoenix_flare")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATURE_WOLF_HOWL = SOUND_EVENTS.register("creature_wolf_howl",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "creature_wolf_howl")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_AIR_CAST = SOUND_EVENTS.register("magic_air_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_air_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_AIR_IMPACT = SOUND_EVENTS.register("magic_air_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_air_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_ARCANE_CHARGE = SOUND_EVENTS.register("magic_arcane_charge",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_arcane_charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_ARCANE_RELEASE = SOUND_EVENTS.register("magic_arcane_release",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_arcane_release")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_DARKNESS_CAST = SOUND_EVENTS.register("magic_darkness_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_darkness_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_DARKNESS_IMPACT = SOUND_EVENTS.register("magic_darkness_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_darkness_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_EARTH_IMPACT = SOUND_EVENTS.register("magic_earth_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_earth_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_EARTH_RISE = SOUND_EVENTS.register("magic_earth_rise",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_earth_rise")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_ETHER_IMPACT = SOUND_EVENTS.register("magic_ether_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_ether_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_FIRE_CAST = SOUND_EVENTS.register("magic_fire_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_fire_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_FIRE_IMPACT = SOUND_EVENTS.register("magic_fire_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_fire_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_HOLY_BURST = SOUND_EVENTS.register("magic_holy_burst",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_holy_burst")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_ICE_CAST = SOUND_EVENTS.register("magic_ice_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_ice_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_ICE_IMPACT = SOUND_EVENTS.register("magic_ice_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_ice_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_LIGHT_CAST = SOUND_EVENTS.register("magic_light_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_light_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_LIGHT_IMPACT = SOUND_EVENTS.register("magic_light_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_light_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_LIGHTNING_CAST = SOUND_EVENTS.register("magic_lightning_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_lightning_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_LIGHTNING_IMPACT = SOUND_EVENTS.register("magic_lightning_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_lightning_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_METAL_CLANG = SOUND_EVENTS.register("magic_metal_clang",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_metal_clang")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_METAL_IMPACT = SOUND_EVENTS.register("magic_metal_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_metal_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_NATURE_CAST = SOUND_EVENTS.register("magic_nature_cast",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_nature_cast")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_NATURE_IMPACT = SOUND_EVENTS.register("magic_nature_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_nature_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_SHADOW_BLINK = SOUND_EVENTS.register("magic_shadow_blink",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_shadow_blink")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_SPIRIT_CALL = SOUND_EVENTS.register("magic_spirit_call",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_spirit_call")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_SPIRIT_IMPACT = SOUND_EVENTS.register("magic_spirit_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_spirit_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_WATER_IMPACT = SOUND_EVENTS.register("magic_water_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_water_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_WATER_WAVE = SOUND_EVENTS.register("magic_water_wave",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_water_wave")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_ALCHEMY_BUBBLE = SOUND_EVENTS.register("profession_alchemy_bubble",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_alchemy_bubble")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_ANVIL_STRIKE = SOUND_EVENTS.register("profession_anvil_strike",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_anvil_strike")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_BOOK_PAGES = SOUND_EVENTS.register("profession_book_pages",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_book_pages")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_CONSTRUCTION_HAMMER = SOUND_EVENTS.register("profession_construction_hammer",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_construction_hammer")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_COOKING_SIZZLE = SOUND_EVENTS.register("profession_cooking_sizzle",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_cooking_sizzle")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_CRAFT_FINISH = SOUND_EVENTS.register("profession_craft_finish",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_craft_finish")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_FORGE_FIRE = SOUND_EVENTS.register("profession_forge_fire",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_forge_fire")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_MINING_PICK = SOUND_EVENTS.register("profession_mining_pick",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_mining_pick")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PROFESSION_SAW_WOOD = SOUND_EVENTS.register("profession_saw_wood",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "profession_saw_wood")));
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_COIN_CLINK = SOUND_EVENTS.register("ui_coin_clink",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui_coin_clink")));
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_DOOR_OPEN = SOUND_EVENTS.register("ui_door_open",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui_door_open")));
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_LEVEL_UP = SOUND_EVENTS.register("ui_level_up",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui_level_up")));
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_QUEST_COMPLETE = SOUND_EVENTS.register("ui_quest_complete",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui_quest_complete")));
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_SKILL_UNLOCK = SOUND_EVENTS.register("ui_skill_unlock",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui_skill_unlock")));

    /** Call from the mod constructor with the mod event bus. */
    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
