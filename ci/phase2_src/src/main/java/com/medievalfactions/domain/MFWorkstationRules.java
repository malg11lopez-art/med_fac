package com.medievalfactions.domain;

import java.util.Map;
import java.util.Set;

/**
 * Small, explicit bridge between starter production recipes and blocks that can host them.
 * A scheduled resident may only start an order at a compatible, loaded workstation.
 */
public final class MFWorkstationRules {
    private static final Map<String, Set<String>> STATIONS_BY_RECIPE = Map.of(
            "cut_planks", Set.of("minecraft:crafting_table"),
            "smelt_iron", Set.of("minecraft:furnace", "minecraft:blast_furnace", "medieval_factions:forge_controller"),
            "smelt_gold", Set.of("minecraft:furnace", "minecraft:blast_furnace", "medieval_factions:forge_controller"),
            "bake_bread", Set.of("minecraft:furnace", "minecraft:smoker", "minecraft:campfire"),
            "forge_iron_sword", Set.of("minecraft:anvil", "minecraft:smithing_table",
                    "medieval_factions:blacksmith_anvil", "medieval_factions:forge_controller"),
            "craft_iron_chestplate", Set.of("minecraft:anvil", "minecraft:smithing_table",
                    "medieval_factions:blacksmith_anvil", "medieval_factions:forge_controller"),
            "cut_stone_bricks", Set.of("minecraft:stonecutter"),
            "shape_stone_stairs", Set.of("minecraft:crafting_table", "minecraft:stonecutter"),
            "smoke_beef", Set.of("minecraft:smoker", "minecraft:furnace"),
            "craft_leather_horse_armor", Set.of("minecraft:crafting_table"),
            "weave_white_carpet", Set.of("minecraft:crafting_table", "minecraft:loom"),
            "craft_redstone_repeater", Set.of("minecraft:crafting_table"),
            "craft_observer", Set.of("minecraft:crafting_table"),
            "brew_healing_tonic", Set.of("minecraft:brewing_stand", "medieval_factions:arcane_workbench"),
            "brew_fire_resistance_tonic", Set.of("minecraft:brewing_stand", "medieval_factions:arcane_workbench")
    );

    private MFWorkstationRules() {}

    public static boolean supports(String recipeId, String blockId) {
        if (recipeId == null || blockId == null
                || !blockId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return false;
        return STATIONS_BY_RECIPE.getOrDefault(recipeId, Set.of()).contains(blockId);
    }

    public static Set<String> acceptedStations(String recipeId) {
        return STATIONS_BY_RECIPE.getOrDefault(recipeId, Set.of());
    }
}
