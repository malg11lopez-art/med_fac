package com.medievalfactions.registry;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.item.MFPotionItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Functional first equipment set; crafting ingredients remain simple stackable items. */
public final class MFItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MedievalFactions.MOD_ID);
    private static final Map<String, DeferredHolder<Item, Item>> REGISTRY = new LinkedHashMap<>();

    private static void simple(String id) {
        REGISTRY.put(id, ITEMS.register(id, () -> new Item(new Item.Properties())));
    }

    private static void sword(String id, Tier tier, int damage, float speed, boolean fireResistant) {
        REGISTRY.put(id, ITEMS.register(id, () -> {
            Item.Properties properties = new Item.Properties()
                    .attributes(SwordItem.createAttributes(tier, damage, speed));
            if (fireResistant) properties.fireResistant();
            return new SwordItem(tier, properties);
        }));
    }

    static {
        // Materials, currency, crafting ingredients and magical reagents.
        String[] materials = {
            "adamantite_ingot", "ancient_wood", "arcane_book", "arcane_dust", "glacial_crystal",
            "coin_copper", "coin_gold", "coin_silver", "leather_roll", "mithril_ingot",
            "rune_crystal", "sunsteel_ingot", "sunsteel_fragment"
        };
        for (String id : materials) simple(id);

        // Proper ammunition and ranged/utility equipment.
        REGISTRY.put("crossbow_bolt", ITEMS.register("crossbow_bolt", () -> new ArrowItem(new Item.Properties())));
        REGISTRY.put("hunter_bow", ITEMS.register("hunter_bow", () -> new BowItem(new Item.Properties().durability(384))));
        REGISTRY.put("fishing_rod", ITEMS.register("fishing_rod", () -> new FishingRodItem(new Item.Properties().durability(64))));

        // Mining and wood-working tools have native tool components, correct block speed and durability.
        REGISTRY.put("iron_axe", ITEMS.register("iron_axe", () -> new AxeItem(Tiers.IRON, new Item.Properties())));
        REGISTRY.put("iron_pickaxe", ITEMS.register("iron_pickaxe", () -> new PickaxeItem(Tiers.IRON, new Item.Properties())));
        REGISTRY.put("stone_shovel", ITEMS.register("stone_shovel", () -> new ShovelItem(Tiers.STONE, new Item.Properties())));
        REGISTRY.put("smith_hammer", ITEMS.register("smith_hammer", () -> new PickaxeItem(Tiers.IRON, new Item.Properties())));

        // Melee equipment. Damage/speed are explicit so each weapon has a predictable combat role.
        sword("iron_sword", Tiers.IRON, 3, -2.4F, false);
        sword("battle_spear", Tiers.IRON, 4, -2.7F, false);
        sword("moon_dagger", Tiers.IRON, 2, -1.7F, false);
        sword("shadow_dagger", Tiers.DIAMOND, 3, -1.5F, false);
        sword("greatsword", Tiers.DIAMOND, 7, -3.2F, false);
        sword("lunar_blade", Tiers.DIAMOND, 5, -2.1F, false);
        sword("solar_blade", Tiers.DIAMOND, 6, -2.5F, true);
        sword("sun_spear", Tiers.DIAMOND, 5, -2.7F, true);
        sword("warhammer", Tiers.DIAMOND, 7, -3.1F, false);
        sword("arcane_staff", Tiers.IRON, 2, -2.8F, false);
        sword("fire_staff", Tiers.DIAMOND, 4, -2.8F, true);
        sword("ice_staff", Tiers.DIAMOND, 4, -2.8F, false);

        REGISTRY.put("orc_shield", ITEMS.register("orc_shield", () -> new ShieldItem(new Item.Properties().durability(420))));
        REGISTRY.put("paladin_shield", ITEMS.register("paladin_shield", () -> new ShieldItem(new Item.Properties().durability(560).fireResistant())));

        // Consumables now have real, server-side effects and consumption behavior.
        REGISTRY.put("healing_potion", ITEMS.register("healing_potion", () -> new MFPotionItem(
                MFPotionItem.EffectKind.HEALING, new Item.Properties().stacksTo(16))));
        REGISTRY.put("fire_potion", ITEMS.register("fire_potion", () -> new MFPotionItem(
                MFPotionItem.EffectKind.FIRE_RESISTANCE, new Item.Properties().stacksTo(16).fireResistant())));

        registerBlockItem("blacksmith_anvil", MFBlocks.BLACKSMITH_ANVIL);
        registerBlockItem("town_hall_brick", MFBlocks.TOWN_HALL_BRICK);
        registerBlockItem("forge_controller", MFBlocks.FORGE_CONTROLLER);
        registerBlockItem("arcane_workbench", MFBlocks.ARCANE_WORKBENCH);
        registerBlockItem("royal_banners", MFBlocks.ROYAL_BANNERS);
        registerBlockItem("windmill", MFBlocks.WINDMILL);
        registerBlockItem("sunsteel_ore", MFBlocks.SUNSTEEL_ORE);
        registerBlockItem("deepslate_sunsteel_ore", MFBlocks.DEEPSLATE_SUNSTEEL_ORE);
    }

    private static void registerBlockItem(String id, Supplier<? extends Block> block) {
        REGISTRY.put(id, ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
    }

    public static Map<String, DeferredHolder<Item, Item>> all() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    private MFItems() {}
}
