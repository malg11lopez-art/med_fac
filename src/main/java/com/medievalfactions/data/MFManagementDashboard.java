package com.medievalfactions.data;

import com.medievalfactions.domain.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Read-only server data model for the eight management GUI pages. */
public final class MFManagementDashboard {
    private static final String[] TITLES = {
            "Resumen del reino", "Habitantes e IA", "Ciudades y obras", "Ejércitos y asedios",
            "Comercio y caravanas", "Dragones y compañeros", "Familia y medicina", "Aventuras y jefes"
    };

    private MFManagementDashboard() {}

    public static String title(int page) {
        return TITLES[Math.max(0, Math.min(TITLES.length - 1, page))];
    }

    public static String render(ServerPlayer player, int requestedPage) {
        if (player == null) return "No hay un jugador conectado.";
        int page = Math.max(0, Math.min(TITLES.length - 1, requestedPage));
        var server = player.serverLevel().getServer();
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = MFFactionSavedData.get(server).factionForPlayer(player.getUUID()).orElse(null);
        List<MFCitizenState> population = world.citizens(null);
        List<MFSettlementState> settlements = world.settlements();

        List<MFWorkOrderState> orders = new ArrayList<>();
        for (MFSettlementState settlement : settlements) {
            orders.addAll(world.workOrders(settlement.id()));
        }
        MFPopulationIndex index = MFPopulationIndex.build(population, orders,
                advanced.records("expedition_journeys", MFAdventureSystems.ExpeditionJourney.class));

        StringBuilder out = new StringBuilder(1_400);
        out.append("Jugador: ").append(player.getGameProfile().getName()).append('\n');
        out.append("Día simulado: ").append(world.simulatedDays()).append(" | Facción: ")
                .append(faction == null ? "sin facción" : faction.name()).append('\n');
        out.append("Población: ").append(index.citizensById().size())
                .append(" | Habitantes activos: ")
                .append(index.activeBySettlement().values().stream().mapToLong(Long::longValue).sum())
                .append(" | Asentamientos: ").append(settlements.size()).append('\n');
        out.append('\n');

        switch (page) {
            case 0 -> overview(out, advanced, population, settlements, faction);
            case 1 -> residents(out, advanced, population, index);
            case 2 -> cities(out, advanced, settlements);
            case 3 -> warfare(out, advanced);
            case 4 -> trade(out, advanced);
            case 5 -> dragons(out, advanced);
            case 6 -> familyAndMedicine(out, advanced);
            case 7 -> adventures(out, advanced);
            default -> out.append("Página no disponible.");
        }
        out.append("\nActualización: ").append(player.serverLevel().getGameTime()).append(" ticks del mundo.");
        if (out.length() > 3_600) {
            return out.substring(0, 3_580) + "\nContenido limitado.";
        }
        return out.toString();
    }

    private static void overview(StringBuilder out, MFAdvancedSavedData data,
                                 List<MFCitizenState> population, List<MFSettlementState> settlements,
                                 MFFactionSavedData.FactionRecord faction) {
        long active = population.stream().filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE).count();
        long lowHealth = population.stream().filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE && c.health() <= 30).count();
        long hungry = population.stream().filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE && c.hunger() >= 70).count();
        out.append("TESORERÍA: ").append(faction == null ? "—" : faction.treasuryCopper() + " cobre").append('\n');
        out.append("Habitantes activos: ").append(active).append(" | Salud crítica: ").append(lowHealth)
                .append(" | Hambre alta: ").append(hungry).append('\n');
        out.append("IA: ").append(data.count("npc_directives")).append(" directivas | ")
                .append(data.count("citizen_schedules")).append(" horarios | ")
                .append(data.count("citizen_plans")).append(" planes\n");
        out.append("Ciudades: ").append(data.count("city_plans")).append(" planes | ")
                .append(data.count("construction")).append(" proyectos de obra\n");
        out.append("Guerra: ").append(data.count("army_columns")).append(" columnas | ")
                .append(data.count("tactical_battles")).append(" batallas | ")
                .append(data.count("sieges")).append(" asedios\n");
        out.append("Comercio: ").append(data.count("caravans")).append(" caravanas | ")
                .append(data.count("physical_convoys")).append(" convoyes físicos | ")
                .append(data.count("bank_accounts")).append(" cuentas\n");
        out.append("Criaturas: ").append(data.count("companions")).append(" compañeros | ")
                .append(data.count("dragon_eggs")).append(" huevos | ")
                .append(data.count("boss_encounters")).append(" jefes\n");
        out.append("Familia/salud: ").append(data.count("marriages")).append(" matrimonios | ")
                .append(data.count("pregnancies")).append(" gestaciones | ")
                .append(data.count("medical_cases")).append(" casos médicos\n");
        out.append("Mundo: ").append(settlements.size()).append(" asentamientos | ")
                .append(data.count("expeditions")).append(" expediciones | ")
                .append(data.count("dungeons")).append(" mazmorras");
    }

    private static void residents(StringBuilder out, MFAdvancedSavedData data,
                                  List<MFCitizenState> population, MFPopulationIndex index) {
        out.append("Horarios: ").append(data.count("citizen_schedules"))
                .append(" | Planes: ").append(data.count("citizen_plans"))
                .append(" | Trabajadores: ").append(index.activeWorkers().size()).append('\n');
        out.append("Expedicionarios: ").append(index.travellingCitizens().size()).append('\n');
        population.stream().filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE)
                .sorted(Comparator.comparingInt(MFCitizenState::health).thenComparing(MFCitizenState::name))
                .limit(9).forEach(c -> {
                    var plan = data.record("citizen_plans", c.id(), MFCitizenSimulation.CitizenPlan.class).orElse(null);
                    out.append(c.name()).append(" | ").append(c.profession())
                            .append(" | salud ").append(c.health()).append(" | hambre ").append(c.hunger())
                            .append(" | ").append(plan == null ? "sin plan" : plan.directive()).append('\n');
                });
        out.append("\nLa IA actualiza los horarios al cambiar el minuto de Minecraft.");
    }

    private static void cities(StringBuilder out, MFAdvancedSavedData data, List<MFSettlementState> settlements) {
        out.append("ASENTAMIENTOS\n");
        settlements.stream().sorted(Comparator.comparing(MFSettlementState::name)).limit(10).forEach(s ->
                out.append(s.name()).append(" | ").append(s.tier()).append(" | seguridad ").append(s.security())
                        .append(" | vivienda ").append(s.housingCapacity()).append(" | comida ")
                        .append(s.foodRations()).append(" (").append(s.foodDays()).append(" días)\n"));
        for (MFCityPlanner.CityPlan plan : data.records("city_plans", MFCityPlanner.CityPlan.class).stream().limit(6).toList()) {
            long pending = plan.buildings().stream().filter(b -> b.stage() != MFCityPlanner.BuildStage.ACTIVE).count();
            out.append("Plan ").append(plan.id().toString(), 0, 8).append(" | edificios ")
                    .append(plan.buildings().size()).append(" | pendientes ").append(pending).append('\n');
        }
        out.append("Obras físicas: ").append(data.count("construction"))
                .append(" | enlaces urbanos: ").append(data.count("city_build_links"));
    }

    private static void warfare(StringBuilder out, MFAdvancedSavedData data) {
        out.append("Columnas: ").append(data.count("army_columns"))
                .append(" | batallas: ").append(data.count("tactical_battles"))
                .append(" | ocupaciones: ").append(data.count("occupations"))
                .append(" | asedios: ").append(data.count("sieges")).append('\n');
        for (MFTacticalWarfare.ArmyColumn army : data.records("army_columns", MFTacticalWarfare.ArmyColumn.class).stream().limit(8).toList()) {
            out.append("Columna ").append(army.id().toString(), 0, 8).append(" | ")
                    .append(army.formation()).append(" | tropas ").append(army.troopCount())
                    .append(" | provisiones ").append(army.provisions()).append(" | moral ").append(army.morale())
                    .append(" | ruta ").append(army.nextWaypoint()).append('/').append(army.route().size()).append('\n');
        }
        for (MFAdvancedSystems.Siege siege : data.records("sieges", MFAdvancedSystems.Siege.class).stream().limit(6).toList()) {
            out.append("Asedio ").append(siege.id().toString(), 0, 8).append(" | ").append(siege.status())
                    .append(" | muro ").append(siege.wallIntegrity()).append("% | días ").append(siege.days()).append('\n');
        }
    }

    private static void trade(StringBuilder out, MFAdvancedSavedData data) {
        out.append("Caravanas: ").append(data.count("caravans"))
                .append(" | convoyes físicos: ").append(data.count("physical_convoys"))
                .append(" | cuentas: ").append(data.count("bank_accounts"))
                .append(" | préstamos: ").append(data.count("loans")).append('\n');
        for (MFAdvancedSystems.Caravan caravan : data.records("caravans", MFAdvancedSystems.Caravan.class).stream().limit(8).toList()) {
            out.append(caravan.status()).append(" | ").append(caravan.quantity()).append(' ')
                    .append(caravan.itemId()).append(" | días ").append(caravan.daysRemaining()).append('\n');
        }
        for (MFAdventureSystems.PhysicalConvoy convoy : data.records("physical_convoys", MFAdventureSystems.PhysicalConvoy.class).stream().limit(6).toList()) {
            out.append("Convoy ").append(convoy.id().toString(), 0, 8).append(" | ").append(convoy.status())
                    .append(" | carga ").append(convoy.remainingQuantity()).append('/')
                    .append(convoy.originalQuantity()).append(" | riesgo ").append(convoy.effectiveRisk()).append('\n');
        }
    }

    private static void dragons(StringBuilder out, MFAdvancedSavedData data) {
        List<MFAdvancedSystems.Companion> companions = data.records("companions", MFAdvancedSystems.Companion.class);
        long dragons = companions.stream().filter(MFAdvancedSystems.Companion::dragon).count();
        out.append("Compañeros: ").append(companions.size()).append(" | dragones: ").append(dragons)
                .append(" | genomas: ").append(data.count("dragon_genomes"))
                .append(" | huevos: ").append(data.count("dragon_eggs"))
                .append(" | crías: ").append(data.count("dragon_hatchlings")).append('\n');
        for (MFAdvancedSystems.Companion companion : companions.stream().limit(8).toList()) {
            out.append(companion.species()).append(" | vínculo ").append(companion.bondLevel())
                    .append(" | salud ").append(companion.health()).append('/')
                    .append(companion.maxHealth()).append(" | hambre ").append(companion.hunger())
                    .append(" | nivel ").append(companion.level()).append('\n');
        }
        for (MFDragonLifecycle.DragonEgg egg : data.records("dragon_eggs", MFDragonLifecycle.DragonEgg.class).stream().limit(4).toList()) {
            out.append("Huevo ").append(egg.id().toString(), 0, 8).append(" | ").append(egg.status())
                    .append(" | incubación ").append(egg.incubationProgress()).append('/')
                    .append(egg.requiredProgress()).append(" | calor ").append(egg.warmth())
                    .append(" | cuidado ").append(egg.care()).append('\n');
        }
    }

    private static void familyAndMedicine(StringBuilder out, MFAdvancedSavedData data) {
        out.append("Personas: ").append(data.count("persons"))
                .append(" | relaciones: ").append(data.count("relationships"))
                .append(" | matrimonios: ").append(data.count("marriages"))
                .append(" | gestaciones: ").append(data.count("pregnancies"))
                .append(" | linajes: ").append(data.count("lineage")).append('\n');
        out.append("Medicina: ").append(data.count("medical_cases"))
                .append(" casos | clínicas ").append(data.count("clinics")).append('\n');
        for (MFFamilyCare.Marriage marriage : data.records("marriages", MFFamilyCare.Marriage.class).stream().limit(6).toList()) {
            out.append("Matrimonio ").append(marriage.id().toString(), 0, 8).append(" | ").append(marriage.status())
                    .append(" | consentimiento ").append(marriage.consentA()).append('/').append(marriage.consentB()).append('\n');
        }
        for (MFFamilyCare.MedicalCase medical : data.records("medical_cases", MFFamilyCare.MedicalCase.class).stream().limit(8).toList()) {
            out.append(medical.condition()).append(" | gravedad ").append(medical.severity())
                    .append(" | infección ").append(medical.infection()).append(" | ").append(medical.status()).append('\n');
        }
    }

    private static void adventures(StringBuilder out, MFAdvancedSavedData data) {
        out.append("Expediciones: ").append(data.count("expeditions"))
                .append(" | rutas físicas: ").append(data.count("expedition_journeys"))
                .append(" | mazmorras: ").append(data.count("dungeons"))
                .append(" | jefes: ").append(data.count("boss_encounters")).append('\n');
        for (MFAdventureSystems.BossEncounter boss : data.records("boss_encounters", MFAdventureSystems.BossEncounter.class).stream().limit(8).toList()) {
            out.append(boss.bossId()).append(" | ").append(boss.phase())
                    .append(" | salud ").append(boss.health()).append('/').append(boss.maxHealth())
                    .append(" | escudo ").append(boss.shield()).append('\n');
        }
        for (MFAdvancedSystems.Expedition expedition : data.records("expeditions", MFAdvancedSystems.Expedition.class).stream().limit(6).toList()) {
            out.append("Expedición ").append(expedition.id().toString(), 0, 8).append(" | ")
                    .append(expedition.status()).append(" | nivel ").append(expedition.dungeonTier())
                    .append(" | provisiones ").append(expedition.provisions()).append('\n');
        }
    }
}
