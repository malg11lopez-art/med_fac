package com.medievalfactions.registry;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.Set;

/** Operator-only sound catalogue used to audition every registered audio asset in-game. */
public final class MFSoundPreview {
    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> SOUNDS = Map.ofEntries(
        Map.entry("ambient_campfire_loop", MFSounds.AMBIENT_CAMPFIRE_LOOP),
        Map.entry("ambient_dungeon_loop", MFSounds.AMBIENT_DUNGEON_LOOP),
        Map.entry("ambient_forest_loop", MFSounds.AMBIENT_FOREST_LOOP),
        Map.entry("ambient_forge_loop", MFSounds.AMBIENT_FORGE_LOOP),
        Map.entry("ambient_tavern_loop", MFSounds.AMBIENT_TAVERN_LOOP),
        Map.entry("ambient_town_day_loop", MFSounds.AMBIENT_TOWN_DAY_LOOP),
        Map.entry("combat_armor_hit", MFSounds.COMBAT_ARMOR_HIT),
        Map.entry("combat_arrow_release", MFSounds.COMBAT_ARROW_RELEASE),
        Map.entry("combat_blade_hit", MFSounds.COMBAT_BLADE_HIT),
        Map.entry("combat_bow_draw", MFSounds.COMBAT_BOW_DRAW),
        Map.entry("combat_crossbow_load", MFSounds.COMBAT_CROSSBOW_LOAD),
        Map.entry("combat_footstep_grass", MFSounds.COMBAT_FOOTSTEP_GRASS),
        Map.entry("combat_footstep_sand", MFSounds.COMBAT_FOOTSTEP_SAND),
        Map.entry("combat_footstep_snow", MFSounds.COMBAT_FOOTSTEP_SNOW),
        Map.entry("combat_footstep_stone", MFSounds.COMBAT_FOOTSTEP_STONE),
        Map.entry("combat_footstep_wood", MFSounds.COMBAT_FOOTSTEP_WOOD),
        Map.entry("combat_jump", MFSounds.COMBAT_JUMP),
        Map.entry("combat_land", MFSounds.COMBAT_LAND),
        Map.entry("combat_parry", MFSounds.COMBAT_PARRY),
        Map.entry("combat_shield_block", MFSounds.COMBAT_SHIELD_BLOCK),
        Map.entry("combat_sword_draw", MFSounds.COMBAT_SWORD_DRAW),
        Map.entry("combat_sword_swing_heavy", MFSounds.COMBAT_SWORD_SWING_HEAVY),
        Map.entry("combat_sword_swing_light", MFSounds.COMBAT_SWORD_SWING_LIGHT),
        Map.entry("creature_dragon_bite", MFSounds.CREATURE_DRAGON_BITE),
        Map.entry("creature_dragon_breath_charge", MFSounds.CREATURE_DRAGON_BREATH_CHARGE),
        Map.entry("creature_dragon_breath_release", MFSounds.CREATURE_DRAGON_BREATH_RELEASE),
        Map.entry("creature_dragon_roar", MFSounds.CREATURE_DRAGON_ROAR),
        Map.entry("creature_dragon_wing_flap", MFSounds.CREATURE_DRAGON_WING_FLAP),
        Map.entry("creature_griffin_screech", MFSounds.CREATURE_GRIFFIN_SCREECH),
        Map.entry("creature_horse_neigh", MFSounds.CREATURE_HORSE_NEIGH),
        Map.entry("creature_horse_step", MFSounds.CREATURE_HORSE_STEP),
        Map.entry("creature_phoenix_cry", MFSounds.CREATURE_PHOENIX_CRY),
        Map.entry("creature_phoenix_flare", MFSounds.CREATURE_PHOENIX_FLARE),
        Map.entry("creature_wolf_howl", MFSounds.CREATURE_WOLF_HOWL),
        Map.entry("magic_air_cast", MFSounds.MAGIC_AIR_CAST),
        Map.entry("magic_air_impact", MFSounds.MAGIC_AIR_IMPACT),
        Map.entry("magic_arcane_charge", MFSounds.MAGIC_ARCANE_CHARGE),
        Map.entry("magic_arcane_release", MFSounds.MAGIC_ARCANE_RELEASE),
        Map.entry("magic_darkness_cast", MFSounds.MAGIC_DARKNESS_CAST),
        Map.entry("magic_darkness_impact", MFSounds.MAGIC_DARKNESS_IMPACT),
        Map.entry("magic_earth_impact", MFSounds.MAGIC_EARTH_IMPACT),
        Map.entry("magic_earth_rise", MFSounds.MAGIC_EARTH_RISE),
        Map.entry("magic_ether_impact", MFSounds.MAGIC_ETHER_IMPACT),
        Map.entry("magic_fire_cast", MFSounds.MAGIC_FIRE_CAST),
        Map.entry("magic_fire_impact", MFSounds.MAGIC_FIRE_IMPACT),
        Map.entry("magic_holy_burst", MFSounds.MAGIC_HOLY_BURST),
        Map.entry("magic_ice_cast", MFSounds.MAGIC_ICE_CAST),
        Map.entry("magic_ice_impact", MFSounds.MAGIC_ICE_IMPACT),
        Map.entry("magic_light_cast", MFSounds.MAGIC_LIGHT_CAST),
        Map.entry("magic_light_impact", MFSounds.MAGIC_LIGHT_IMPACT),
        Map.entry("magic_lightning_cast", MFSounds.MAGIC_LIGHTNING_CAST),
        Map.entry("magic_lightning_impact", MFSounds.MAGIC_LIGHTNING_IMPACT),
        Map.entry("magic_metal_clang", MFSounds.MAGIC_METAL_CLANG),
        Map.entry("magic_metal_impact", MFSounds.MAGIC_METAL_IMPACT),
        Map.entry("magic_nature_cast", MFSounds.MAGIC_NATURE_CAST),
        Map.entry("magic_nature_impact", MFSounds.MAGIC_NATURE_IMPACT),
        Map.entry("magic_shadow_blink", MFSounds.MAGIC_SHADOW_BLINK),
        Map.entry("magic_spirit_call", MFSounds.MAGIC_SPIRIT_CALL),
        Map.entry("magic_spirit_impact", MFSounds.MAGIC_SPIRIT_IMPACT),
        Map.entry("magic_water_impact", MFSounds.MAGIC_WATER_IMPACT),
        Map.entry("magic_water_wave", MFSounds.MAGIC_WATER_WAVE),
        Map.entry("profession_alchemy_bubble", MFSounds.PROFESSION_ALCHEMY_BUBBLE),
        Map.entry("profession_anvil_strike", MFSounds.PROFESSION_ANVIL_STRIKE),
        Map.entry("profession_book_pages", MFSounds.PROFESSION_BOOK_PAGES),
        Map.entry("profession_construction_hammer", MFSounds.PROFESSION_CONSTRUCTION_HAMMER),
        Map.entry("profession_cooking_sizzle", MFSounds.PROFESSION_COOKING_SIZZLE),
        Map.entry("profession_craft_finish", MFSounds.PROFESSION_CRAFT_FINISH),
        Map.entry("profession_forge_fire", MFSounds.PROFESSION_FORGE_FIRE),
        Map.entry("profession_mining_pick", MFSounds.PROFESSION_MINING_PICK),
        Map.entry("profession_saw_wood", MFSounds.PROFESSION_SAW_WOOD),
        Map.entry("ui_coin_clink", MFSounds.UI_COIN_CLINK),
        Map.entry("ui_door_open", MFSounds.UI_DOOR_OPEN),
        Map.entry("ui_level_up", MFSounds.UI_LEVEL_UP),
        Map.entry("ui_quest_complete", MFSounds.UI_QUEST_COMPLETE),
        Map.entry("ui_skill_unlock", MFSounds.UI_SKILL_UNLOCK)
    );

    private MFSoundPreview() {}

    public static DeferredHolder<SoundEvent, SoundEvent> find(String id) {
        return SOUNDS.get(id);
    }

    public static Set<String> ids() {
        return SOUNDS.keySet();
    }
}
