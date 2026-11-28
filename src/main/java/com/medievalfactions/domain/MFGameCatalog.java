package com.medievalfactions.domain;

import java.util.List;

/** Canonical, versioned vocabulary shared by character, settlement and simulation systems. */
public final class MFGameCatalog {
    private MFGameCatalog() {}

    public enum Affinity { SOLAR, LUNAR }

    public enum Element {
        FIRE(Affinity.SOLAR), LIGHT(Affinity.SOLAR), LIGHTNING(Affinity.SOLAR),
        EARTH(Affinity.SOLAR), METAL(Affinity.SOLAR), AETHER(Affinity.SOLAR),
        WATER(Affinity.LUNAR), ICE(Affinity.LUNAR), AIR(Affinity.LUNAR),
        DARKNESS(Affinity.LUNAR), NATURE(Affinity.LUNAR), SPIRIT(Affinity.LUNAR);

        private final Affinity affinity;
        Element(Affinity affinity) { this.affinity = affinity; }
        public Affinity affinity() { return affinity; }
    }

    public enum RaceFamily {
        HUMANS, ELVES, DWARVES, ORCS, BEASTMEN, GOBLINS, DRACONIDS, VAMPIRES,
        UNDEAD, FAE, CELESTIALS, DEMONS, MERFOLK, CENTAURS, GIANTS, HARPIES
    }

    public enum CombatClass {
        WARRIOR, ARCHER, CROSSBOWMAN, MAGE, PALADIN, ASSASSIN, BERSERKER,
        DRUID, NECROMANCER, MONK, SUMMONER, BARD, ARCANE_SWORDSMAN, CLERIC, LANCER
    }

    public enum Profession {
        MINER, LUMBERJACK, STONECUTTER, PROSPECTOR, BLACKSMITH, ARMORER, SMELTER,
        CARPENTER, RANGED_ARTISAN, LEATHERWORKER, TAILOR, JEWELER, MAGICAL_ARTISAN,
        ALCHEMIST, MASON, BAKER, BUTCHER, MILLER, ENGINEER, FARMER, RANCHER,
        SHEPHERD, HORSE_BREEDER, APICULTURIST, FISHER, TRADER, APPRAISER,
        WHOLESALER, BANKER, TREASURER, MINT_MASTER, ACCOUNTANT, TAX_COLLECTOR,
        LENDER, AUCTIONEER, BUILDER, ARCHITECT, SHIPWRIGHT, TRANSPORTER,
        CARTWRIGHT, CARAVANER, MESSENGER, CARTOGRAPHER, LIBRARIAN, SCRIBE,
        TEACHER, RESEARCHER, ARCANE_RESEARCHER, ELEMENTAL_MASTER, DOCTOR,
        SURGEON, VETERINARIAN, DIPLOMAT, JUDGE, GUARD, PRIEST, SETTLEMENT_ADMINISTRATOR,
        CAPTAIN
    }

    public enum SettlementTier { CAMP, VILLAGE, TOWN, VILLA, CITY, CAPITAL }

    public enum Season { REBIRTH, ASCENSION, SPLENDOR, DECLINE, FROST, DORMANCY }

    public enum DayName { AUREN, VALDOR, MYRIEN, KAELOR, THARION, ELYNDRA, VAELIS, NOXAR }

    public enum AgeStage { BABY, CHILD, ADOLESCENT, ADULT, ELDER }

    public enum CitizenStatus { ACTIVE, AWAY, LEFT, DEAD }

    public enum Treaty { NON_AGGRESSION, ALLIANCE, TRADE, TRANSIT, ESCORT, MILITARY_AID, TRUCE, PEACE, ECONOMIC_AGREEMENT, TRIBUTE }

    public static List<Element> elementsFor(Affinity affinity) {
        return affinity == Affinity.SOLAR
                ? List.of(Element.FIRE, Element.LIGHT, Element.LIGHTNING, Element.EARTH, Element.METAL, Element.AETHER)
                : List.of(Element.WATER, Element.ICE, Element.AIR, Element.DARKNESS, Element.NATURE, Element.SPIRIT);
    }

    public static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().replace('-', '_').replace(' ', '_').toUpperCase(java.util.Locale.ROOT);
        try { return Enum.valueOf(type, normalized); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
