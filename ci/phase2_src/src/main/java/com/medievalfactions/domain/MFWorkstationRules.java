package com.medievalfactions.domain;

import java.util.Map;
import java.util.Set;

/**
 * Small, explicit bridge between starter production recipes and blocks that can host them.
 * A scheduled resident may only start an order at a compatible, loaded workstation.
 */
public final class MFWorkstationRules {
    private static final Map<String, Set<String>> STATIONS_BY_RECIPE = Map.ofEntries(
            Map.entry("cut_planks", Set.of("minecraft:crafting_table")),
            Map.entry("smelt_iron", Set.of("minecraft:furnace", "minecraft:blast_furnace", "medieval_factions:forge_controller")),
            Map.entry("smelt_gold", Set.of("minecraft:furnace", "minecraft:blast_furnace", "medieval_factions:forge_controller")),
            Map.entry("bake_bread", Set.of("minecraft:furnace", "minecraft:smoker", "minecraft:campfire")),
            Map.entry("forge_iron_sword", Set.of("minecraft:anvil", "minecraft:smithing_table",
                    "medieval_factions:blacksmith_anvil", "medieval_factions:forge_controller")),
            Map.entry("craft_iron_chestplate", Set.of("minecraft:anvil", "minecraft:smithing_table",
                    "medieval_factions:blacksmith_anvil", "medieval_factions:forge_controller")),
            Map.entry("cut_stone_bricks", Set.of("minecraft:stonecutter")),
            Map.entry("shape_stone_stairs", Set.of("minecraft:crafting_table", "minecraft:stonecutter")),
            Map.entry("smoke_beef", Set.of("minecraft:smoker", "minecraft:furnace")),
            Map.entry("craft_leather_horse_armor", Set.of("minecraft:crafting_table")),
            Map.entry("weave_white_carpet", Set.of("minecraft:crafting_table", "minecraft:loom")),
            Map.entry("craft_redstone_repeater", Set.of("minecraft:crafting_table")),
            Map.entry("craft_observer", Set.of("minecraft:crafting_table")),
            Map.entry("craft_bow", Set.of("minecraft:crafting_table")),
            Map.entry("craft_minecart", Set.of("minecraft:crafting_table")),
            Map.entry("craft_oak_boat", Set.of("minecraft:crafting_table")),
            Map.entry("craft_clock", Set.of("minecraft:crafting_table")),
            Map.entry("brew_healing_tonic", Set.of("minecraft:brewing_stand", "medieval_factions:arcane_workbench")),
            Map.entry("brew_fire_resistance_tonic", Set.of("minecraft:brewing_stand", "medieval_factions:arcane_workbench"))
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
