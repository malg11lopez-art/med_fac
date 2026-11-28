package com.medievalfactions.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.medievalfactions.domain.MFGameCatalog.CombatClass;

/** Data-driven combat talent trees. All classes have 55-60 nodes; Bard has two longer branches. */
public final class MFSkillTreeCatalog {
    public enum NodeKind { CORE, STAT, PASSIVE, ACTIVE, ACTIVE_UPGRADE, SUBCLASS_GATE, SLOT, MASTERY }
    public record Branch(String id, String displayName, String subclassId, String subclassName) {}
    public record Node(String id, CombatClass combatClass, String branchId, int branchDepth,
                       NodeKind kind, String displayName, Set<String> prerequisites, int minimumClassLevel) {}

    private static final Map<CombatClass, List<Branch>> BRANCHES = new EnumMap<>(CombatClass.class);
    private static final Map<CombatClass, Map<String, Node>> NODES = new EnumMap<>(CombatClass.class);
    private static final Map<String, Branch> SUBCLASSES = new LinkedHashMap<>();
    private static final int CORE_NODES = 6;
    private static final int STANDARD_BRANCH_DEPTH = 18;
    private static final int BARD_BRANCH_DEPTH = 25;

    static {
        define(CombatClass.WARRIOR, b("vanguard", "Vanguardia", "warrior.vanguard", "Vanguardia"), b("guardian", "Guardián", "warrior.guardian", "Guardián"), b("weaponmaster", "Maestro de armas", "warrior.weaponmaster", "Maestro de armas"));
        define(CombatClass.ARCHER, b("ranger", "Explorador", "archer.ranger", "Explorador"), b("marksman", "Tirador", "archer.marksman", "Tirador"), b("beasthunter", "Cazabestias", "archer.beasthunter", "Cazabestias"));
        define(CombatClass.CROSSBOWMAN, b("sharpshooter", "Certero", "crossbowman.sharpshooter", "Certero"), b("siege", "Especialista de asedio", "crossbowman.siege", "Especialista de asedio"), b("trapper", "Trampero", "crossbowman.trapper", "Trampero"));
        define(CombatClass.MAGE, b("elementalist", "Elementalista", "mage.elementalist", "Elementalista"), b("arcanist", "Arcanista", "mage.arcanist", "Arcanista"), b("spellblade", "Hoja arcana", "mage.spellblade", "Hoja arcana"));
        define(CombatClass.PALADIN, b("templar", "Templario", "paladin.templar", "Templario"), b("bulwark", "Baluarte", "paladin.bulwark", "Baluarte"), b("inquisitor", "Inquisidor", "paladin.inquisitor", "Inquisidor"));
        define(CombatClass.ASSASSIN, b("shadow", "Sombra", "assassin.shadow", "Sombra"), b("venom", "Envenenador", "assassin.venom", "Envenenador"), b("duelist", "Duelista", "assassin.duelist", "Duelista"));
        define(CombatClass.BERSERKER, b("ravager", "Devastador", "berserker.ravager", "Devastador"), b("bloodreaver", "Segador sangriento", "berserker.bloodreaver", "Segador sangriento"), b("juggernaut", "Imparable", "berserker.juggernaut", "Imparable"));
        define(CombatClass.DRUID, b("warden", "Guardián natural", "druid.warden", "Guardián natural"), b("shifter", "Metamorfo", "druid.shifter", "Metamorfo"), b("spiritcaller", "Llamador espiritual", "druid.spiritcaller", "Llamador espiritual"));
        define(CombatClass.NECROMANCER, b("lich", "Liche", "necromancer.lich", "Liche"), b("reaper", "Segador", "necromancer.reaper", "Segador"), b("bonecommander", "Comandante óseo", "necromancer.bonecommander", "Comandante óseo"));
        define(CombatClass.MONK, b("ascetic", "Asceta", "monk.ascetic", "Asceta"), b("fist", "Puño interior", "monk.fist", "Puño interior"), b("mystic", "Místico", "monk.mystic", "Místico"));
        define(CombatClass.SUMMONER, b("conjurer", "Conjurador", "summoner.conjurer", "Conjurador"), b("beastmaster", "Domabestias", "summoner.beastmaster", "Domabestias"), b("pactbinder", "Pactista", "summoner.pactbinder", "Pactista"));
        define(CombatClass.BARD, b("support", "Bardo de apoyo", "bard.support", "Bardo de apoyo"), b("physical", "Bardo de impulso físico", "bard.physical", "Bardo de impulso físico"));
        define(CombatClass.ARCANE_SWORDSMAN, b("spellknight", "Caballero hechicero", "arcane_swordsman.spellknight", "Caballero hechicero"), b("runeblade", "Hoja rúnica", "arcane_swordsman.runeblade", "Hoja rúnica"), b("fencer", "Espadachín místico", "arcane_swordsman.fencer", "Espadachín místico"));
        define(CombatClass.CLERIC, b("healer", "Sanador", "cleric.healer", "Sanador"), b("exorcist", "Exorcista", "cleric.exorcist", "Exorcista"), b("oracle", "Oráculo", "cleric.oracle", "Oráculo"));
        define(CombatClass.LANCER, b("dragoon", "Dragón de guerra", "lancer.dragoon", "Dragón de guerra"), b("cavalier", "Caballero montado", "lancer.cavalier", "Caballero montado"), b("phalanx", "Falange", "lancer.phalanx", "Falange"));
        for (CombatClass combatClass : CombatClass.values()) buildTree(combatClass);
    }

    private MFSkillTreeCatalog() {}
    private static Branch b(String id, String label, String subclassId, String subclassName) {
        return new Branch(id, label, subclassId, subclassName);
    }
    private static void define(CombatClass combatClass, Branch... branches) {
        BRANCHES.put(combatClass, List.of(branches));
        for (Branch branch : branches) SUBCLASSES.put(branch.subclassId(), branch);
    }

    private static void buildTree(CombatClass combatClass) {
        LinkedHashMap<String, Node> nodes = new LinkedHashMap<>();
        String previous = null;
        for (int i = 1; i <= CORE_NODES; i++) {
            String id = combatClass.name().toLowerCase() + ".core_" + String.format(java.util.Locale.ROOT, "%02d", i);
            Set<String> prerequisites = previous == null ? Set.of() : Set.of(previous);
            nodes.put(id, new Node(id, combatClass, "core", i, NodeKind.CORE,
                    "Núcleo " + i, prerequisites, 1));
            previous = id;
        }
        String coreLast = previous;
        for (Branch branch : BRANCHES.get(combatClass)) {
            int depth = combatClass == CombatClass.BARD ? BARD_BRANCH_DEPTH : STANDARD_BRANCH_DEPTH;
            previous = coreLast;
            for (int i = 1; i <= depth; i++) {
                NodeKind kind = switch (i) {
                    case 1 -> NodeKind.SUBCLASS_GATE;
                    case 6, 13, 20 -> NodeKind.SLOT;
                    case 2, 7, 14, 19, 23 -> NodeKind.STAT;
                    case 3, 8, 11, 16, 21 -> NodeKind.PASSIVE;
                    case 4, 9, 15, 22 -> NodeKind.ACTIVE;
                    case 5, 10, 17 -> NodeKind.ACTIVE_UPGRADE;
                    default -> i == depth ? NodeKind.MASTERY : NodeKind.PASSIVE;
                };
                if (i == depth) kind = NodeKind.MASTERY;
                String id = combatClass.name().toLowerCase() + "." + branch.id() + "." + String.format(java.util.Locale.ROOT, "%02d", i);
                Set<String> prerequisites = Set.of(previous);
                int minLevel = i == 1 ? 15 : Math.min(65, 15 + 2 * (i - 1));
                String label = branch.displayName() + " · " + kindLabel(kind) + " " + i;
                nodes.put(id, new Node(id, combatClass, branch.id(), i, kind, label, prerequisites, minLevel));
                previous = id;
            }
        }
        NODES.put(combatClass, Collections.unmodifiableMap(nodes));
    }

    private static String kindLabel(NodeKind kind) {
        return switch (kind) {
            case CORE -> "Centro"; case STAT -> "Atributo"; case PASSIVE -> "Pasiva";
            case ACTIVE -> "Habilidad"; case ACTIVE_UPGRADE -> "Mejora";
            case SUBCLASS_GATE -> "Subclase"; case SLOT -> "Espacio de habilidad"; case MASTERY -> "Maestría";
        };
    }

    public static List<Branch> branches(CombatClass combatClass) { return BRANCHES.getOrDefault(combatClass, List.of()); }
    public static Map<String, Node> nodes(CombatClass combatClass) { return NODES.getOrDefault(combatClass, Map.of()); }
    public static Optional<Node> node(CombatClass combatClass, String id) { return Optional.ofNullable(nodes(combatClass).get(id)); }
    public static Optional<Branch> subclass(String id) { return Optional.ofNullable(SUBCLASSES.get(id)); }
    public static List<Node> visibleTree(CombatClass combatClass) { return new ArrayList<>(nodes(combatClass).values()); }
    public static boolean isCoreNode(String id) { return id != null && id.contains(".core_"); }
    public static Set<String> starterCoreNodes(CombatClass combatClass) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Node node : nodes(combatClass).values()) if (node.kind() == NodeKind.CORE) result.add(node.id());
        return Collections.unmodifiableSet(result);
    }
    public static Optional<Node> subclassGate(CombatClass combatClass, String subclassId) {
        Branch branch = SUBCLASSES.get(subclassId);
        if (branch == null || !branches(combatClass).contains(branch)) return Optional.empty();
        return nodes(combatClass).values().stream().filter(n -> n.branchId().equals(branch.id()) && n.kind() == NodeKind.SUBCLASS_GATE).findFirst();
    }
}
