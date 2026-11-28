package com.medievalfactions.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.medievalfactions.domain.MFGameCatalog.Profession;

/** Starter production recipes. Inputs are real inventory items; recipes never create resources ex nihilo. */
public final class MFRecipeCatalog {
    public record Recipe(String id, Profession profession, Map<String, Long> inputs,
                         Map<String, Long> outputs, int durationDays, long professionXp) {
        public Recipe {
            if (id == null || !id.matches("[a-z0-9_.-]{2,48}") || profession == null
                    || inputs == null || inputs.isEmpty() || outputs == null || outputs.isEmpty()
                    || durationDays < 1 || professionXp < 1) throw new IllegalArgumentException("Invalid recipe");
            inputs = Map.copyOf(inputs); outputs = Map.copyOf(outputs);
            inputs.forEach((k,v) -> validateItem(k,v)); outputs.forEach((k,v) -> validateItem(k,v));
        }
        private static void validateItem(String id, Long amount) {
            if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || amount == null || amount < 1)
                throw new IllegalArgumentException("Invalid recipe item");
        }
    }

    private static final Map<String, Recipe> RECIPES;
    static {
        LinkedHashMap<String, Recipe> recipes = new LinkedHashMap<>();
        add(recipes, new Recipe("smelt_iron", Profession.SMELTER,
                Map.of("minecraft:raw_iron", 1L, "minecraft:coal", 1L), Map.of("minecraft:iron_ingot", 1L), 1, 35L));
        add(recipes, new Recipe("smelt_gold", Profession.SMELTER,
                Map.of("minecraft:raw_gold", 1L, "minecraft:coal", 1L), Map.of("minecraft:gold_ingot", 1L), 1, 40L));
        add(recipes, new Recipe("bake_bread", Profession.BAKER,
                Map.of("minecraft:wheat", 3L), Map.of("minecraft:bread", 1L), 1, 30L));
        add(recipes, new Recipe("cut_planks", Profession.CARPENTER,
                Map.of("minecraft:oak_log", 1L), Map.of("minecraft:oak_planks", 4L), 1, 25L));
        add(recipes, new Recipe("forge_iron_sword", Profession.BLACKSMITH,
                Map.of("minecraft:iron_ingot", 2L, "minecraft:stick", 1L), Map.of("minecraft:iron_sword", 1L), 2, 90L));
        add(recipes, new Recipe("craft_iron_chestplate", Profession.ARMORER,
                Map.of("minecraft:iron_ingot", 8L), Map.of("minecraft:iron_chestplate", 1L), 3, 120L));
        add(recipes, new Recipe("cut_stone_bricks", Profession.STONECUTTER,
                Map.of("minecraft:stone", 1L), Map.of("minecraft:stone_bricks", 2L), 1, 30L));
        add(recipes, new Recipe("shape_stone_stairs", Profession.MASON,
                Map.of("minecraft:stone_bricks", 6L), Map.of("minecraft:stone_brick_stairs", 4L), 1, 35L));
        add(recipes, new Recipe("smoke_beef", Profession.BUTCHER,
                Map.of("minecraft:raw_beef", 1L, "minecraft:coal", 1L), Map.of("minecraft:cooked_beef", 1L), 1, 30L));
        add(recipes, new Recipe("craft_leather_horse_armor", Profession.LEATHERWORKER,
                Map.of("minecraft:leather", 7L), Map.of("minecraft:leather_horse_armor", 1L), 2, 65L));
        add(recipes, new Recipe("weave_white_carpet", Profession.TAILOR,
                Map.of("minecraft:white_wool", 2L), Map.of("minecraft:white_carpet", 3L), 1, 35L));
        add(recipes, new Recipe("craft_redstone_repeater", Profession.ENGINEER,
                Map.of("minecraft:redstone_torch", 2L, "minecraft:redstone", 1L, "minecraft:stone", 3L),
                Map.of("minecraft:repeater", 1L), 1, 55L));
        add(recipes, new Recipe("craft_observer", Profession.ENGINEER,
                Map.of("minecraft:cobblestone", 6L, "minecraft:redstone", 2L, "minecraft:quartz", 1L),
                Map.of("minecraft:observer", 1L), 2, 75L));
        RECIPES = Map.copyOf(recipes);
    }

    private MFRecipeCatalog() {}
    private static void add(Map<String, Recipe> map, Recipe recipe) { map.put(recipe.id(), recipe); }
    public static Optional<Recipe> recipe(String id) { return Optional.ofNullable(RECIPES.get(id)); }
    public static List<Recipe> all() { return List.copyOf(RECIPES.values()); }
}
