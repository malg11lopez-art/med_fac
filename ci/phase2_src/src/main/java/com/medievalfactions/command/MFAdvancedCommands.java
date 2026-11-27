package com.medievalfactions.command;

import com.medievalfactions.registry.MFSoundPreview;

import net.minecraft.commands.SharedSuggestionProvider;

import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.data.MFWorldSavedData;
import com.medievalfactions.data.MFFactionSavedData;
import com.medievalfactions.domain.MFAdvancedSystems;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFRaceBiology;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.medievalfactions.registry.MFEntities;
import com.medievalfactions.entity.MFAnimatedMob;
import com.medievalfactions.entity.MFResidentEntity;
import com.medievalfactions.domain.MFCitizenSimulation;
import com.medievalfactions.entity.MFDragonEntity;
import com.medievalfactions.entity.MFWolfEntity;
import com.medievalfactions.entity.MFGriffinEntity;
import com.medievalfactions.entity.MFPhoenixEntity;
import com.medievalfactions.economy.MFCurrency;
import com.medievalfactions.registry.MFItems;
import com.medievalfactions.registry.MFSounds;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

import static com.medievalfactions.domain.MFAdvancedSystems.*;
import static com.medievalfactions.domain.MFGameCatalog.*;

/** In-game admin and player commands for advanced systems. */
public final class MFAdvancedCommands {
    private MFAdvancedCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        var root = Commands.literal("advanced");
        root.then(npcCommands());
        root.then(warfareCommands());
        root.then(bossCommands());
        root.then(caravanCommands());
        root.then(bankCommands());
        root.then(siegeCommands());
        root.then(constructionCommands());
        root.then(cityCommands());
        root.then(companionCommands());
        root.then(familyCommands());
        root.then(expeditionCommands());
        root.then(dungeonCommands());
        root.then(soundCommands());
        root.then(skillCommands());
        root.then(statusCommand());
        root.then(dashboardCommand());
        return root;
    }

    /** Server-authoritative tactical battles and strategic army columns. */
    private static LiteralArgumentBuilder<CommandSourceStack> warfareCommands() {
        var command = Commands.literal("warfare").requires(source -> source.hasPermission(2));
        var unitsEach = Commands.argument("units_each", IntegerArgumentType.integer(1, 16)).executes(ctx -> {
            UUID attacker = uuid(ctx, "attacker_faction");
            UUID defender = uuid(ctx, "defender_faction");
            int each = IntegerArgumentType.getInteger(ctx, "units_each");
            var factions = MFFactionSavedData.get(ctx.getSource().getServer());
            if (attacker == null || defender == null || attacker.equals(defender)
                    || factions.getFaction(attacker).isEmpty() || factions.getFaction(defender).isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Batalla rechazada: las dos facciones deben existir y ser distintas."));
                return 0;
            }
            UUID battleId = UUID.randomUUID();
            List<com.medievalfactions.domain.MFTacticalWarfare.Unit> units = new ArrayList<>();
            for (int i = 0; i < each; i++) {
                units.add(new com.medievalfactions.domain.MFTacticalWarfare.Unit(
                        UUID.randomUUID(), attacker, i % 4, i / 4, 100, 100, 6, 18, 2,
                        75, 75, 2, com.medievalfactions.domain.MFTacticalWarfare.Stance.READY, false));
                units.add(new com.medievalfactions.domain.MFTacticalWarfare.Unit(
                        UUID.randomUUID(), defender, 8 + i % 4, i / 4, 100, 100, 8, 15, 2,
                        80, 80, 2, com.medievalfactions.domain.MFTacticalWarfare.Stance.READY, false));
            }
            var battle = new com.medievalfactions.domain.MFTacticalWarfare.Battle(
                    battleId, attacker, defender, attacker, 1, units,
                    com.medievalfactions.domain.MFTacticalWarfare.BattleStatus.ACTIVE, null);
            boolean stored = MFAdvancedSavedData.get(ctx.getSource().getServer())
                    .storeSimulationRecord("tactical_battles", battleId, battle,
                            com.medievalfactions.domain.MFTacticalWarfare.Battle.class);
            if (!stored) { ctx.getSource().sendFailure(Component.literal("No se pudo persistir el combate táctico.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Batalla creada: " + battleId
                    + " | unidades por bando " + each + " | turno de la facción atacante."), true);
            return 1;
        });
        var defenderFaction = Commands.argument("defender_faction", StringArgumentType.word()).then(unitsEach);
        var attackerFaction = Commands.argument("attacker_faction", StringArgumentType.word()).then(defenderFaction);
        command.then(Commands.literal("battle").then(Commands.literal("create").then(attackerFaction)));
        command.then(Commands.literal("battle").then(Commands.literal("list").executes(ctx -> {
            var battles = MFAdvancedSavedData.get(ctx.getSource().getServer()).records(
                    "tactical_battles", com.medievalfactions.domain.MFTacticalWarfare.Battle.class);
            for (var battle : battles) ctx.getSource().sendSuccess(() -> Component.literal(
                    battle.id() + " | " + battle.status() + " | turno " + battle.turn()
                            + " | activo " + battle.activeFaction() + " | unidades " + battle.units().size()), false);
            return battles.size();
        })));
        command.then(Commands.literal("battle").then(Commands.literal("end")
                .then(Commands.argument("battle_id", StringArgumentType.word()).executes(ctx -> {
                    UUID id = uuid(ctx, "battle_id");
                    var updated = MFAdvancedSavedData.get(ctx.getSource().getServer()).endTacticalTurn(id);
                    if (updated.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se puede finalizar el turno: batalla inexistente o concluida.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Turno " + updated.get().turn()
                            + " | ahora actúa " + updated.get().activeFaction()), true);
                    return 1;
                }))));
        var orderZ = Commands.argument("destination_z", IntegerArgumentType.integer()).executes(ctx -> {
            UUID battleId = uuid(ctx, "battle_id");
            UUID actorId = uuid(ctx, "actor_id");
            UUID targetId = parseUuid(StringArgumentType.getString(ctx, "target_id"));
            var action = parseEnum(com.medievalfactions.domain.MFTacticalWarfare.Action.class,
                    StringArgumentType.getString(ctx, "action"));
            if (action == null) { ctx.getSource().sendFailure(Component.literal("Acción inválida: MOVE, ATTACK, BLOCK, DODGE, RALLY o RETREAT.")); return 0; }
            var order = new com.medievalfactions.domain.MFTacticalWarfare.Order(actorId, action, targetId,
                    IntegerArgumentType.getInteger(ctx, "destination_x"), IntegerArgumentType.getInteger(ctx, "destination_z"));
            var result = MFAdvancedSavedData.get(ctx.getSource().getServer()).applyTacticalOrder(battleId, order);
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Orden rechazada: verifica turno, puntos de acción, objetivo y alcance.")); return 0; }
            var value = result.get();
            ctx.getSource().sendSuccess(() -> Component.literal(value.message() + " | daño " + value.damage()
                    + " | estado " + value.battle().status()), true);
            return 1;
        });
        var orderX = Commands.argument("destination_x", IntegerArgumentType.integer()).then(orderZ);
        var target = Commands.argument("target_id", StringArgumentType.word()).then(orderX);
        var action = Commands.argument("action", StringArgumentType.word()).then(target);
        var actor = Commands.argument("actor_id", StringArgumentType.word()).then(action);
        command.then(Commands.literal("battle").then(Commands.literal("order")
                .then(Commands.argument("battle_id", StringArgumentType.word()).then(actor))));

        var armyDestination = Commands.argument("destination", BlockPosArgument.blockPos()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID factionId = uuid(ctx, "faction_id");
            UUID commanderId = uuid(ctx, "commander_id");
            int troopCount = IntegerArgumentType.getInteger(ctx, "troop_count");
            long provisions = LongArgumentType.getLong(ctx, "provisions");
            var formation = parseEnum(com.medievalfactions.domain.MFTacticalWarfare.Formation.class,
                    StringArgumentType.getString(ctx, "formation"));
            var faction = MFFactionSavedData.get(ctx.getSource().getServer()).getFaction(factionId).orElse(null);
            var citizen = MFWorldSavedData.get(ctx.getSource().getServer()).citizen(commanderId).orElse(null);
            boolean commanderValid = faction != null && (faction.playerMembers().contains(commanderId)
                    || (citizen != null && citizen.status() == MFGameCatalog.CitizenStatus.ACTIVE
                        && citizen.factionId().equals(factionId)));
            if (!commanderValid || formation == null || troopCount < 1 || troopCount > 100_000 || provisions < 1) {
                ctx.getSource().sendFailure(Component.literal("Columna rechazada: comandante de la facción, formación, efectivos o provisiones inválidos."));
                return 0;
            }
            BlockPos destinationPos = BlockPosArgument.getLoadedBlockPos(ctx, "destination");
            BlockPos origin = player.blockPosition();
            String dimension = player.serverLevel().dimension().location().toString();
            List<MFCitizenSimulation.Waypoint> route = List.of(
                    new MFCitizenSimulation.Waypoint(dimension, origin.getX(), origin.getY(), origin.getZ()),
                    new MFCitizenSimulation.Waypoint(dimension, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ()));
            int trackedUnits = Math.min(256, troopCount);
            List<UUID> unitIds = new ArrayList<>(trackedUnits);
            for (int i=0;i<trackedUnits;i++) unitIds.add(UUID.randomUUID());
            UUID columnId = UUID.randomUUID();
            var column = new com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn(
                    columnId, factionId, commanderId, unitIds, formation, troopCount, provisions, 75,
                    route, 0, MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
            boolean stored = MFAdvancedSavedData.get(ctx.getSource().getServer()).storeSimulationRecord(
                    "army_columns", columnId, column, com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn.class);
            if (!stored) { ctx.getSource().sendFailure(Component.literal("No se pudo guardar la columna militar.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Columna desplegada: " + columnId
                    + " | tropas " + troopCount + " | formación " + formation + " | ruta " + route.size() + " puntos."), true);
            return 1;
        });
        var armyFormation = Commands.argument("formation", StringArgumentType.word()).then(armyDestination);
        var armyProvisions = Commands.argument("provisions", LongArgumentType.longArg(1)).then(armyFormation);
        var armyTroops = Commands.argument("troop_count", IntegerArgumentType.integer(1, 100_000)).then(armyProvisions);
        var armyCommander = Commands.argument("commander_id", StringArgumentType.word()).then(armyTroops);
        var armyFaction = Commands.argument("faction_id", StringArgumentType.word()).then(armyCommander);
        command.then(Commands.literal("army").then(Commands.literal("muster").then(armyFaction)));
        command.then(Commands.literal("army").then(Commands.literal("list").executes(ctx -> {
            var columns = MFAdvancedSavedData.get(ctx.getSource().getServer()).records(
                    "army_columns", com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn.class);
            for (var army : columns) ctx.getSource().sendSuccess(() -> Component.literal(
                    army.id() + " | " + army.troopCount() + " tropas | " + army.formation()
                            + " | provisiones " + army.provisions() + " | waypoint "
                            + army.nextWaypoint() + "/" + army.route().size()), false);
            return columns.size();
        })));
        return command;
    }

    /** Real Wither-boss encounter linked to persisted phases, deterministic telegraphs and party damage. */
    private static LiteralArgumentBuilder<CommandSourceStack> bossCommands() {
        var command = Commands.literal("boss").requires(source -> source.hasPermission(2));
        command.then(Commands.literal("spawn").then(Commands.argument("boss_id", StringArgumentType.word())
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                .then(Commands.argument("seed", LongArgumentType.longArg()).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    UUID encounterId = UUID.randomUUID();
                    long day = MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays();
                    var encounter = com.medievalfactions.domain.MFAdventureSystems.createBoss(
                            encounterId, StringArgumentType.getString(ctx, "boss_id"),
                            IntegerArgumentType.getInteger(ctx, "tier"), LongArgumentType.getLong(ctx, "seed"));
                    WitherBoss physical = EntityType.WITHER.create(player.serverLevel());
                    if (physical == null) { ctx.getSource().sendFailure(Component.literal("No se pudo crear la entidad de jefe.")); return 0; }
                    var maxHealth = physical.getAttribute(Attributes.MAX_HEALTH);
                    if (maxHealth != null) maxHealth.setBaseValue(encounter.maxHealth());
                    physical.setHealth(encounter.maxHealth());
                    physical.moveTo(player.getX() + player.getLookAngle().x * 6.0D, player.getY(),
                            player.getZ() + player.getLookAngle().z * 6.0D, player.getYRot(), 0.0F);
                    physical.setCustomName(Component.literal("Jefe: " + encounter.bossId() + " · Nivel " + encounter.tier()));
                    physical.setCustomNameVisible(true);
                    physical.setPersistenceRequired();
                    physical.getPersistentData().putUUID("mf_boss_encounter", encounterId);
                    boolean saved = MFAdvancedSavedData.get(ctx.getSource().getServer()).storeSimulationRecord(
                            "boss_encounters", encounterId, encounter,
                            com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class);
                    boolean linked = saved && MFAdvancedSavedData.get(ctx.getSource().getServer()).storeSimulationRecord(
                            "boss_entities", encounterId,
                            new com.medievalfactions.domain.MFAdventureSystems.BossEntityLink(encounterId,
                                    physical.getUUID(), player.serverLevel().dimension().location().toString(),
                                    player.getUUID(), day),
                            com.medievalfactions.domain.MFAdventureSystems.BossEntityLink.class);
                    if (!linked || !player.serverLevel().addFreshEntity(physical)) {
                        physical.discard();
                        if (saved) MFAdvancedSavedData.get(ctx.getSource().getServer()).removeSimulationRecord("boss_encounters", encounterId);
                        MFAdvancedSavedData.get(ctx.getSource().getServer()).removeSimulationRecord("boss_entities", encounterId);
                        ctx.getSource().sendFailure(Component.literal("No se pudo persistir o desplegar el jefe; no se ha consumido ningún recurso."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Jefe desplegado: " + encounter.bossId()
                            + " | encuentro " + encounterId + " | salud " + encounter.health()
                            + " | barra de vida de Minecraft activa."), true);
                    return 1;
                })))));
        command.then(Commands.literal("list").executes(ctx -> {
            var bosses = MFAdvancedSavedData.get(ctx.getSource().getServer()).records(
                    "boss_encounters", com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class);
            for (var boss : bosses) ctx.getSource().sendSuccess(() -> Component.literal(
                    boss.id() + " | " + boss.bossId() + " | " + boss.phase() + " | "
                            + boss.health() + "/" + boss.maxHealth() + " | escudo " + boss.shield()
                            + " | turno " + boss.turn()), false);
            return bosses.size();
        }));
        command.then(Commands.literal("turn").then(Commands.argument("encounter_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = uuid(ctx, "encounter_id");
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var link = data.record("boss_entities", id, com.medievalfactions.domain.MFAdventureSystems.BossEntityLink.class).orElse(null);
            if (link == null) { ctx.getSource().sendFailure(Component.literal("No existe el jefe físico vinculado a ese encuentro.")); return 0; }
            ServerLevel level = null;
            for (ServerLevel candidateLevel : ctx.getSource().getServer().getAllLevels()) {
                if (candidateLevel.dimension().location().toString().equals(link.dimension())) {
                    level = candidateLevel;
                    break;
                }
            }
            Entity entity = level == null ? null : level.getEntity(link.entityId());
            if (!(entity instanceof WitherBoss physical) || !physical.isAlive()) {
                ctx.getSource().sendFailure(Component.literal("La entidad del jefe no está cargada o ha sido derrotada."));
                return 0;
            }
            List<ServerPlayer> party = level.getEntitiesOfClass(ServerPlayer.class, physical.getBoundingBox().inflate(18.0D));
            int defence = party.stream().mapToInt(ServerPlayer::getArmorValue).max().orElse(0);
            var result = data.advanceBossEncounter(id, defence).orElse(null);
            if (result == null) { ctx.getSource().sendFailure(Component.literal("El encuentro ya está concluido.")); return 0; }
            for (ServerPlayer member : party) {
                member.hurt(member.damageSources().magic(), result.damageToParty());
                member.displayClientMessage(Component.literal("§c" + result.telegraph() + " | daño recibido: " + result.damageToParty()), true);
            }
            if (result.summonedMinions() > 0) {
                for (int i=0;i<Math.min(4,result.summonedMinions());i++) {
                    Entity minion = EntityType.ZOMBIE.create(level);
                    if (minion == null) continue;
                    minion.moveTo(physical.getX() + (i-1.5D)*2.0D, physical.getY(), physical.getZ() + 3.0D,
                            physical.getYRot(), 0.0F);
                    level.addFreshEntity(minion);
                }
            }
            physical.setHealth(Math.min(physical.getMaxHealth(), physical.getHealth() + result.bossHealing()));
            if (result.shieldChange() > 0) physical.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    Math.min(200, result.shieldChange() * 2), 0));
            physical.playSound(SoundEvents.WITHER_AMBIENT, 1.0F, 0.8F + physical.getRandom().nextFloat() * 0.3F);
            ctx.getSource().sendSuccess(() -> Component.literal("Jefe: " + result.action() + " | " + result.telegraph()
                    + " | invocaciones " + result.summonedMinions() + " | fase " + result.encounter().phase()), true);
            return 1;
        })));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> npcCommands() {
        var command = Commands.literal("npc").requires(source -> source.hasPermission(2));
        command.then(Commands.literal("plan").executes(ctx -> {
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            long day = world.simulatedDays();
            var advanced = MFAdvancedSavedData.get(ctx.getSource().getServer());
            int count = advanced.planNpcPopulation(world, day);
            int minute = (int)((ctx.getSource().getLevel().getDayTime() % 24_000L) * 1_440L / 24_000L);
            int scheduled = advanced.updateScheduledNpcPlans(world, minute);
            ctx.getSource().sendSuccess(() -> Component.literal("Plan de IA actualizado para " + count + " habitantes; horarios activos: " + scheduled + ". La prioridad de supervivencia prevalece sobre el trabajo."), true);
            return count + scheduled;
        }));
        var scheduleCommand = Commands.literal("schedule");
        var citizenArg = Commands.argument("citizen_id", StringArgumentType.word());
        var homeArg = Commands.argument("home", BlockPosArgument.blockPos());
        var workArg = Commands.argument("work", BlockPosArgument.blockPos());
        var foodArg = Commands.argument("food", BlockPosArgument.blockPos());
        var clinicArg = Commands.argument("clinic", BlockPosArgument.blockPos());
        clinicArg.executes(ctx -> {
            UUID citizenId = uuid(ctx, "citizen_id");
            if (citizenId == null) { ctx.getSource().sendFailure(Component.literal("UUID de ciudadano no válido.")); return 0; }
            var server = ctx.getSource().getServer();
            var world = MFWorldSavedData.get(server);
            var level = ctx.getSource().getLevel();
            String dimension = level.dimension().location().toString();
            BlockPos homePos = BlockPosArgument.getLoadedBlockPos(ctx, "home");
            BlockPos workPos = BlockPosArgument.getLoadedBlockPos(ctx, "work");
            BlockPos foodPos = BlockPosArgument.getLoadedBlockPos(ctx, "food");
            BlockPos clinicPos = BlockPosArgument.getLoadedBlockPos(ctx, "clinic");
            var home = new MFCitizenSimulation.Waypoint(dimension, homePos.getX(), homePos.getY(), homePos.getZ());
            var work = new MFCitizenSimulation.Waypoint(dimension, workPos.getX(), workPos.getY(), workPos.getZ());
            var food = new MFCitizenSimulation.Waypoint(dimension, foodPos.getX(), foodPos.getY(), foodPos.getZ());
            var clinic = new MFCitizenSimulation.Waypoint(dimension, clinicPos.getX(), clinicPos.getY(), clinicPos.getZ());
            var schedule = new MFCitizenSimulation.CitizenSchedule(citizenId, List.of(
                    new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.SLEEP, home),
                    new MFCitizenSimulation.ScheduleEntry(360, MFCitizenSimulation.Activity.EAT, food),
                    new MFCitizenSimulation.ScheduleEntry(420, MFCitizenSimulation.Activity.WORK, work),
                    new MFCitizenSimulation.ScheduleEntry(720, MFCitizenSimulation.Activity.EAT, food),
                    new MFCitizenSimulation.ScheduleEntry(780, MFCitizenSimulation.Activity.WORK, work),
                    new MFCitizenSimulation.ScheduleEntry(1020, MFCitizenSimulation.Activity.SOCIALIZE, food),
                    new MFCitizenSimulation.ScheduleEntry(1260, MFCitizenSimulation.Activity.VISIT_MEDIC, clinic),
                    new MFCitizenSimulation.ScheduleEntry(1320, MFCitizenSimulation.Activity.SLEEP, home)));
            if (!MFAdvancedSavedData.get(server).saveCitizenSchedule(world, schedule)) {
                ctx.getSource().sendFailure(Component.literal("Horario rechazado: el ciudadano no existe, no está activo o la identidad no coincide."));
                return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Horario diario guardado para " + citizenId + ". El habitante usará navegación terrestre hacia los destinos."), true);
            return 1;
        });
        foodArg.then(clinicArg);
        workArg.then(foodArg);
        homeArg.then(workArg);
        citizenArg.then(homeArg);
        scheduleCommand.then(citizenArg);
        command.then(scheduleCommand);
        command.then(Commands.literal("spawn").then(Commands.argument("citizen_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID citizenId = uuid(ctx, "citizen_id");
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var citizen = world.citizens(null).stream().filter(c -> c.id().equals(citizenId) && c.status() == CitizenStatus.ACTIVE).findFirst().orElse(null);
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            if (citizen == null || data.record("resident_links", citizenId, MFCitizenSimulation.ResidentEntityLink.class).isPresent()) {
                ctx.getSource().sendFailure(Component.literal("No se pudo crear el habitante: ciudadano inexistente/inactivo o ya tiene una entidad vinculada."));
                return 0;
            }
            var level = player.serverLevel();
            MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
            if (resident == null) { ctx.getSource().sendFailure(Component.literal("No se pudo crear la entidad residente.")); return 0; }
            var spawn = player.blockPosition().relative(player.getDirection().getClockWise());
            resident.moveTo(spawn.getX()+0.5D, spawn.getY(), spawn.getZ()+0.5D, player.getYRot(), 0.0F);
            resident.bindCitizen(citizenId);
            resident.setPersistenceRequired();
            if (!level.addFreshEntity(resident)) { resident.discard(); ctx.getSource().sendFailure(Component.literal("El mundo rechazó la entidad residente.")); return 0; }
            var link = new MFCitizenSimulation.ResidentEntityLink(citizenId, resident.getUUID(), level.dimension().location().toString());
            if (!data.storeSimulationRecord("resident_links", citizenId, link, MFCitizenSimulation.ResidentEntityLink.class)) {
                resident.discard(); ctx.getSource().sendFailure(Component.literal("No se pudo registrar el vínculo persistente.")); return 0;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Habitante físico creado para " + citizen.name() + ". Configura su horario con /mf advanced npc schedule."), true);
            return 1;
        })));
        command.then(Commands.literal("list").executes(ctx -> {
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var directives = data.records("npc_directives", NpcDirective.class);
            ctx.getSource().sendSuccess(() -> Component.literal("Directivas NPC: " + directives.size()), false);
            for (NpcDirective d : directives)
                ctx.getSource().sendSuccess(() -> Component.literal(d.citizenId() + " | " + d.intent() + " | prioridad " + d.priority() + " | " + d.reason()), false);
            return directives.size();
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> caravanCommands() {
        var command = Commands.literal("caravan").requires(source -> source.hasPermission(2));
        var item = Commands.argument("item_id", StringArgumentType.word())
                .then(Commands.argument("quantity", LongArgumentType.longArg(1))
                .then(Commands.argument("unit_price_copper", LongArgumentType.longArg(0))
                .then(Commands.argument("travel_days", IntegerArgumentType.integer(1, 365))
                .then(Commands.argument("route_risk", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
                    UUID seller = uuid(ctx, "seller_id"), buyer = uuid(ctx, "buyer_id");
                    var result = MFAdvancedSavedData.get(ctx.getSource().getServer()).dispatchCaravan(
                            MFWorldSavedData.get(ctx.getSource().getServer()), seller, buyer,
                            StringArgumentType.getString(ctx, "item_id"), LongArgumentType.getLong(ctx, "quantity"),
                            LongArgumentType.getLong(ctx, "unit_price_copper"), IntegerArgumentType.getInteger(ctx, "travel_days"),
                            IntegerArgumentType.getInteger(ctx, "route_risk"), MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
                    if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Caravana rechazada: revisa stock del vendedor, saldo del comprador y los IDs.")); return 0; }
                    var c = result.get();
                    ctx.getSource().sendSuccess(() -> Component.literal("Caravana enviada: " + c.id() + " | " + c.quantity() + " " + c.itemId() + " | llegada en " + c.daysRemaining() + " días."), true);
                    return 1;
                })))));
        command.then(Commands.literal("dispatch").then(Commands.argument("seller_id", StringArgumentType.word())
                .then(Commands.argument("buyer_id", StringArgumentType.word()).then(item))));
        var physicalDestination = Commands.argument("destination", BlockPosArgument.blockPos()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos destination = BlockPosArgument.getLoadedBlockPos(ctx, "destination");
            String dimension = player.serverLevel().dimension().location().toString();
            var route = List.of(new com.medievalfactions.domain.MFAdventureSystems.RoutePoint(dimension,
                            player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ()),
                    new com.medievalfactions.domain.MFAdventureSystems.RoutePoint(dimension,
                            destination.getX(), destination.getY(), destination.getZ()));
            var convoy = MFAdvancedSavedData.get(ctx.getSource().getServer()).dispatchPhysicalConvoy(
                    MFWorldSavedData.get(ctx.getSource().getServer()), uuid(ctx, "seller_id"), uuid(ctx, "buyer_id"),
                    StringArgumentType.getString(ctx, "item_id"), LongArgumentType.getLong(ctx, "quantity"),
                    LongArgumentType.getLong(ctx, "escrow_copper"), route,
                    IntegerArgumentType.getInteger(ctx, "escort_strength"), IntegerArgumentType.getInteger(ctx, "route_risk"),
                    LongArgumentType.getLong(ctx, "seed"));
            if (convoy.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Caravana física rechazada: falta stock, tesorería, ruta válida o IDs.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Caravana física enviada: " + convoy.get().id()
                    + " | mercancía reservada " + convoy.get().remainingQuantity() + " | un punto de ruta por día."), true);
            return 1;
        });
        var physicalSeed = Commands.argument("seed", LongArgumentType.longArg()).then(physicalDestination);
        var physicalRisk = Commands.argument("route_risk", IntegerArgumentType.integer(0, 100)).then(physicalSeed);
        var physicalEscort = Commands.argument("escort_strength", IntegerArgumentType.integer(0, 100)).then(physicalRisk);
        var physicalEscrow = Commands.argument("escrow_copper", LongArgumentType.longArg(0)).then(physicalEscort);
        var physicalQuantity = Commands.argument("quantity", LongArgumentType.longArg(1)).then(physicalEscrow);
        var physicalItem = Commands.argument("item_id", StringArgumentType.word()).then(physicalQuantity);
        var physicalBuyer = Commands.argument("buyer_id", StringArgumentType.word()).then(physicalItem);
        var physicalSeller = Commands.argument("seller_id", StringArgumentType.word()).then(physicalBuyer);
        command.then(Commands.literal("physical").then(physicalSeller));
        command.then(Commands.literal("resume").then(Commands.argument("convoy_id", StringArgumentType.word()).executes(ctx -> {
            var resumed = MFAdvancedSavedData.get(ctx.getSource().getServer()).resumePhysicalConvoy(uuid(ctx, "convoy_id"));
            if (resumed.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No existe una caravana detenida por emboscada.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Caravana reanudada con " + resumed.get().remainingQuantity() + " unidades."), true);
            return 1;
        })));
        command.then(Commands.literal("list-physical").executes(ctx -> {
            var rows = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("physical_convoys",
                    com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy.class);
            for (var c : rows) ctx.getSource().sendSuccess(() -> Component.literal(c.id() + " | " + c.status()
                    + " | " + c.remainingQuantity() + "/" + c.originalQuantity() + " " + c.itemId()
                    + " | escolta " + c.escortStrength() + " | riesgo " + c.effectiveRisk()), false);
            return rows.size();
        }));
        command.then(Commands.literal("list").executes(ctx -> {
            var rows = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("caravans", Caravan.class);
            ctx.getSource().sendSuccess(() -> Component.literal("Caravanas estratégicas registradas: " + rows.size()), false);
            for (Caravan c : rows) ctx.getSource().sendSuccess(() -> Component.literal(c.id() + " | " + c.status() + " | " + c.quantity() + " " + c.itemId() + " | días " + c.daysRemaining()), false);
            return rows.size();
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bankCommands() {
        var command = Commands.literal("bank");
        command.then(Commands.literal("open").then(Commands.argument("branch_settlement_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID branch = uuid(ctx, "branch_settlement_id");
            var result = MFAdvancedSavedData.get(ctx.getSource().getServer()).openBankAccount(player.getUUID(), branch,
                    MFWorldSavedData.get(ctx.getSource().getServer()));
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se pudo abrir: sucursal inexistente o ya tienes una cuenta allí.")); return 0; }
            var a = result.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Cuenta abierta: " + a.id() + " | saldo 0 cobre."), false);
            return 1;
        })));
        command.then(Commands.literal("deposit").then(Commands.argument("account_id", StringArgumentType.word())
                .then(Commands.argument("amount_copper", LongArgumentType.longArg(1)).executes(ctx -> bankMovement(ctx, true)))));
        command.then(Commands.literal("withdraw").then(Commands.argument("account_id", StringArgumentType.word())
                .then(Commands.argument("amount_copper", LongArgumentType.longArg(1)).executes(ctx -> bankMovement(ctx, false)))));
        command.then(Commands.literal("transfer").then(Commands.argument("source_account", StringArgumentType.word())
                .then(Commands.argument("target_account", StringArgumentType.word())
                .then(Commands.argument("amount_copper", LongArgumentType.longArg(1)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    UUID from = uuid(ctx, "source_account"), to = uuid(ctx, "target_account");
                    var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
                    var source = data.record("bank_accounts", from, BankAccount.class);
                    if (source.isEmpty() || !source.get().ownerId().equals(player.getUUID())) {
                        ctx.getSource().sendFailure(Component.literal("Solo el titular puede ordenar transferencias desde esa cuenta.")); return 0;
                    }
                    var result = data.transferBank(from, to, LongArgumentType.getLong(ctx, "amount_copper"));
                    if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Transferencia rechazada: saldo insuficiente, cuenta congelada, sucursales distintas o desbordamiento.")); return 0; }
                    player.playNotifySound(MFSounds.UI_COIN_CLINK.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
                    ctx.getSource().sendSuccess(() -> Component.literal("Transferidos " + result.get().amountCopper() + " cobres."), false);
                    return 1;
                })))));
        command.then(Commands.literal("balance").then(Commands.argument("account_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID id = uuid(ctx, "account_id");
            var account = MFAdvancedSavedData.get(ctx.getSource().getServer()).record("bank_accounts", id, BankAccount.class);
            if (account.isEmpty() || !account.get().ownerId().equals(player.getUUID())) { ctx.getSource().sendFailure(Component.literal("Cuenta inexistente o no te pertenece.")); return 0; }
            var a = account.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Cuenta " + a.id() + " | titular " + a.ownerId() + " | saldo " + a.balanceCopper() + " cobre | " + (a.frozen() ? "congelada" : "activa")), false);
            return 1;
        })));
        command.then(Commands.literal("loan").then(Commands.argument("account_id", StringArgumentType.word())
                .then(Commands.argument("principal_copper", LongArgumentType.longArg(1))
                .then(Commands.argument("interest_bps", IntegerArgumentType.integer(0, 100_000)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    UUID id = uuid(ctx, "account_id");
                    var account = MFAdvancedSavedData.get(ctx.getSource().getServer()).record("bank_accounts", id, BankAccount.class);
                    if (account.isEmpty() || !account.get().ownerId().equals(player.getUUID())) { ctx.getSource().sendFailure(Component.literal("Cuenta inexistente o no te pertenece.")); return 0; }
                    long principal = LongArgumentType.getLong(ctx, "principal_copper");
                    var payout = MFCurrency.breakdown(principal).orElseThrow();
                    Inventory inventory = player.getInventory();
                    Item copper = MFItems.all().get("coin_copper").get();
                    Item silver = MFItems.all().get("coin_silver").get();
                    Item gold = MFItems.all().get("coin_gold").get();
                    if (!canFitCoins(inventory, gold, payout.gold(), silver, payout.silver(), copper, payout.copper())) {
                        ctx.getSource().sendFailure(Component.literal("No hay espacio en el inventario para recibir el préstamo en monedas físicas.")); return 0;
                    }
                    var loan = MFAdvancedSavedData.get(ctx.getSource().getServer()).issueLoan(id, account.get().branchSettlement(),
                            principal, IntegerArgumentType.getInteger(ctx, "interest_bps"),
                            MFWorldSavedData.get(ctx.getSource().getServer()));
                    if (loan.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Préstamo rechazado: la sucursal no dispone de reservas suficientes o el interés desborda el límite.")); return 0; }
                    addCoins(inventory, gold, payout.gold());
                    addCoins(inventory, silver, payout.silver());
                    addCoins(inventory, copper, payout.copper());
                    player.playNotifySound(MFSounds.UI_COIN_CLINK.get(), SoundSource.PLAYERS, 0.8F, 0.92F);
                    ctx.getSource().sendSuccess(() -> Component.literal("Préstamo entregado en efectivo. ID " + loan.get().id() + " | deuda total " + loan.get().totalDueCopper() + " cobres. Deposita monedas y usa /mf advanced bank repay para amortizar desde tu saldo."), true);
                    return 1;
                })))));
        command.then(Commands.literal("repay").then(Commands.argument("loan_id", StringArgumentType.word())
                .then(Commands.argument("amount_copper", LongArgumentType.longArg(1)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    UUID id = uuid(ctx, "loan_id");
                    var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
                    var debt = data.record("loans", id, Loan.class);
                    var borrower = debt.map(d -> data.record("bank_accounts", d.borrowerAccount(), BankAccount.class).orElse(null)).orElse(null);
                    if (borrower == null || !borrower.ownerId().equals(player.getUUID())) {
                        ctx.getSource().sendFailure(Component.literal("Préstamo inexistente o no te pertenece.")); return 0;
                    }
                    var loan = data.repayLoan(id, LongArgumentType.getLong(ctx, "amount_copper"), MFWorldSavedData.get(ctx.getSource().getServer()));
                    if (loan.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Pago rechazado: revisa saldo de cuenta y deuda pendiente.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Pago aplicado. Deuda restante: " + loan.get().outstandingCopper() + " cobres."), false);
                    return 1;
                }))));
        return command;
    }

    private static int bankMovement(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, boolean deposit) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        UUID id = uuid(ctx, "account_id"); long amount = LongArgumentType.getLong(ctx, "amount_copper");
        var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
        var account = data.record("bank_accounts", id, BankAccount.class);
        if (account.isEmpty() || !account.get().ownerId().equals(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("Cuenta inexistente o no te pertenece.")); return 0;
        }
        Inventory inventory = player.getInventory();
        Item copper = MFItems.all().get("coin_copper").get();
        Item silver = MFItems.all().get("coin_silver").get();
        Item gold = MFItems.all().get("coin_gold").get();
        if (deposit) {
            var payment = MFCurrency.payment(amount, inventory.countItem(gold), inventory.countItem(silver), inventory.countItem(copper));
            if (payment.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("No tienes monedas exactas suficientes. Se aceptan cobres, platas (10) y oros (100); no se da cambio automático."));
                return 0;
            }
            var result = data.deposit(MFWorldSavedData.get(ctx.getSource().getServer()), id, amount);
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Depósito rechazado: cuenta congelada o desbordamiento de saldo/reserva.")); return 0; }
            removeCoins(inventory, gold, payment.get().gold());
            removeCoins(inventory, silver, payment.get().silver());
            removeCoins(inventory, copper, payment.get().copper());
        } else {
            var payout = MFCurrency.breakdown(amount).orElseThrow();
            if (!canFitCoins(inventory, gold, payout.gold(), silver, payout.silver(), copper, payout.copper())) {
                ctx.getSource().sendFailure(Component.literal("No hay espacio suficiente en el inventario para las monedas retiradas."));
                return 0;
            }
            var result = data.withdraw(MFWorldSavedData.get(ctx.getSource().getServer()), id, amount);
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Retirada rechazada: saldo de cuenta o reserva líquida insuficientes.")); return 0; }
            addCoins(inventory, gold, payout.gold());
            addCoins(inventory, silver, payout.silver());
            addCoins(inventory, copper, payout.copper());
        }
        player.playNotifySound(MFSounds.UI_COIN_CLINK.get(), SoundSource.PLAYERS, 0.8F, deposit ? 0.96F : 1.06F);
        ctx.getSource().sendSuccess(() -> Component.literal((deposit ? "Depositados " : "Retirados ") + amount + " cobres. "
                + "Saldo de cuenta: " + (data.record("bank_accounts", id, BankAccount.class).map(BankAccount::balanceCopper).orElse(0L))
                + " | reserva de sucursal actualizada."), false);
        return 1;
    }

    private static boolean canFitCoins(Inventory inventory, Item gold, long goldCount,
                                       Item silver, long silverCount, Item copper, long copperCount) {
        long slotsNeeded = slotsNeeded(inventory, gold, goldCount)
                + slotsNeeded(inventory, silver, silverCount)
                + slotsNeeded(inventory, copper, copperCount);
        long emptySlots = 0L;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
            if (inventory.getItem(slot).isEmpty()) emptySlots++;
        return slotsNeeded <= emptySlots;
    }

    private static long slotsNeeded(Inventory inventory, Item item, long amount) {
        if (amount <= 0L) return 0L;
        long existingCapacity = 0L;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) existingCapacity += Math.max(0, stack.getMaxStackSize() - stack.getCount());
        }
        long remainder = Math.max(0L, amount - existingCapacity);
        int defaultStackLimit = new ItemStack(item).getMaxStackSize();
        return (remainder + defaultStackLimit - 1L) / defaultStackLimit;
    }

    private static void removeCoins(Inventory inventory, Item item, long amount) {
        long remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0L; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) continue;
            int taken = (int) Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        inventory.setChanged();
    }

    private static void addCoins(Inventory inventory, Item item, long amount) {
        long remaining = amount;
        int defaultStackLimit = new ItemStack(item).getMaxStackSize();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0L; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item) || stack.getCount() >= stack.getMaxStackSize()) continue;
            int added = (int) Math.min(remaining, stack.getMaxStackSize() - stack.getCount());
            stack.grow(added);
            remaining -= added;
        }
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0L; slot++) {
            if (!inventory.getItem(slot).isEmpty()) continue;
            int added = (int) Math.min(remaining, defaultStackLimit);
            inventory.setItem(slot, new ItemStack(item, added));
            remaining -= added;
        }
        inventory.setChanged();
        if (remaining != 0L) throw new IllegalStateException("Currency inventory capacity changed after preflight");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> siegeCommands() {
        var command = Commands.literal("siege").requires(source -> source.hasPermission(2));
        var suppliesDef = Commands.argument("defender_supplies", LongArgumentType.longArg(0)).executes(ctx -> {
            UUID attacker = uuid(ctx, "attacker_faction"), defender = uuid(ctx, "defender_faction"), settlement = uuid(ctx, "settlement_id");
            var siege = MFAdvancedSavedData.get(ctx.getSource().getServer()).beginSiege(attacker, defender, settlement,
                    IntegerArgumentType.getInteger(ctx, "attackers"), IntegerArgumentType.getInteger(ctx, "defenders"),
                    LongArgumentType.getLong(ctx, "attacker_supplies"), LongArgumentType.getLong(ctx, "defender_supplies"),
                    MFWorldSavedData.get(ctx.getSource().getServer()));
            if (siege.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Asedio rechazado: hace falta guerra declarada y un asentamiento del defensor sin asedio activo.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Asedio iniciado: " + siege.get().id() + " | muro " + siege.get().wallIntegrity() + "%"), true);
            return 1;
        });
        var suppliesAttack = Commands.argument("attacker_supplies", LongArgumentType.longArg(0)).then(suppliesDef);
        var defenders = Commands.argument("defenders", IntegerArgumentType.integer(0, 100_000)).then(suppliesAttack);
        var attackers = Commands.argument("attackers", IntegerArgumentType.integer(1, 100_000)).then(defenders);
        var settlement = Commands.argument("settlement_id", StringArgumentType.word()).then(attackers);
        var defender = Commands.argument("defender_faction", StringArgumentType.word()).then(settlement);
        command.then(Commands.literal("start").then(Commands.argument("attacker_faction", StringArgumentType.word()).then(defender)));
        command.then(Commands.literal("list").executes(ctx -> {
            var sieges = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("sieges", Siege.class);
            for (Siege s : sieges) ctx.getSource().sendSuccess(() -> Component.literal(s.id() + " | " + s.status() + " | muro " + s.wallIntegrity() + "% | atacantes " + s.attackers() + " | defensores " + s.defenders() + " | días " + s.days()), false);
            return sieges.size();
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cityCommands() {
        var command = Commands.literal("city").requires(source -> source.hasPermission(2));

        var planMaxZ = Commands.argument("max_z", IntegerArgumentType.integer()).executes(ctx -> {
            ctx.getSource().getPlayerOrException();
            UUID settlementId = uuid(ctx, "settlement_id");
            var plan = new com.medievalfactions.domain.MFCityPlanner.CityPlan(UUID.randomUUID(), settlementId,
                    ctx.getSource().getLevel().dimension().location().toString(),
                    new com.medievalfactions.domain.MFCityPlanner.Bounds(
                            IntegerArgumentType.getInteger(ctx, "min_x"), IntegerArgumentType.getInteger(ctx, "max_x"),
                            IntegerArgumentType.getInteger(ctx, "min_z"), IntegerArgumentType.getInteger(ctx, "max_z")),
                    List.of(), 0);
            boolean created = MFAdvancedSavedData.get(ctx.getSource().getServer()).createCityPlan(
                    MFWorldSavedData.get(ctx.getSource().getServer()), plan);
            if (!created) { ctx.getSource().sendFailure(Component.literal("No se pudo crear el plan: comprueba el asentamiento y los límites.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Plan urbano creado: " + plan.id() + " | parcela "
                    + plan.bounds().minX() + ".." + plan.bounds().maxX() + ", " + plan.bounds().minZ() + ".." + plan.bounds().maxZ()), true);
            return 1;
        });
        var planMinZ = Commands.argument("min_z", IntegerArgumentType.integer()).then(planMaxZ);
        var planMaxX = Commands.argument("max_x", IntegerArgumentType.integer()).then(planMinZ);
        var planMinX = Commands.argument("min_x", IntegerArgumentType.integer()).then(planMaxX);
        var planSettlement = Commands.argument("settlement_id", StringArgumentType.word()).then(planMinX);
        command.then(Commands.literal("plan").then(planSettlement));

        var buildRotation = Commands.argument("rotation", IntegerArgumentType.integer(0, 3)).executes(ctx -> {
            var player = ctx.getSource().getPlayerOrException();
            UUID planId = uuid(ctx, "plan_id");
            String blueprintId = StringArgumentType.getString(ctx, "blueprint");
            Blueprint blueprint = starterBlueprints().get(blueprintId);
            if (blueprint == null) { ctx.getSource().sendFailure(Component.literal("Plano desconocido. Disponibles: small_house y watchtower.")); return 0; }
            int minX = blueprint.placements().stream().mapToInt(BlockPlacement::x).min().orElse(0);
            int maxX = blueprint.placements().stream().mapToInt(BlockPlacement::x).max().orElse(0);
            int minZ = blueprint.placements().stream().mapToInt(BlockPlacement::z).min().orElse(0);
            int maxZ = blueprint.placements().stream().mapToInt(BlockPlacement::z).max().orElse(0);
            var spec = new com.medievalfactions.domain.MFCityPlanner.BuildingSpec(blueprintId,
                    blueprintId.equals("watchtower") ? com.medievalfactions.domain.MFCityPlanner.District.MILITARY
                            : com.medievalfactions.domain.MFCityPlanner.District.RESIDENTIAL,
                    maxX-minX+1, maxZ-minZ+1, blueprint.placements().size(), blueprint.materialCosts());
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var placed = data.placeCityBuilding(world, planId, spec, StringArgumentType.getString(ctx, "building_id"),
                    IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"), IntegerArgumentType.getInteger(ctx, "rotation"));
            if (placed.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Parcela rechazada: revisa límites, solapamiento y materiales disponibles.")); return 0; }
            var project = data.queueCityBuilding(planId, StringArgumentType.getString(ctx, "building_id"), player.getUUID(), world.simulatedDays());
            if (project.isEmpty()) { ctx.getSource().sendFailure(Component.literal("La parcela quedó reservada, pero no se pudo encolar el plano físico; revisa el registro.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Construcción urbana en marcha: " + project.get().id()
                    + " | " + blueprintId + " | materiales descontados una sola vez | bloques colocados por lotes."), true);
            return 1;
        });
        var buildZ = Commands.argument("z", IntegerArgumentType.integer()).then(buildRotation);
        var buildY = Commands.argument("y", IntegerArgumentType.integer()).then(buildZ);
        var buildX = Commands.argument("x", IntegerArgumentType.integer()).then(buildY);
        var buildBlueprint = Commands.argument("blueprint", StringArgumentType.word()).then(buildX);
        var buildId = Commands.argument("building_id", StringArgumentType.word()).then(buildBlueprint);
        var buildPlan = Commands.argument("plan_id", StringArgumentType.word()).then(buildId);
        command.then(Commands.literal("build").then(buildPlan));

        command.then(Commands.literal("list").then(Commands.argument("plan_id", StringArgumentType.word()).executes(ctx -> {
            UUID planId = uuid(ctx, "plan_id");
            var plan = MFAdvancedSavedData.get(ctx.getSource().getServer()).record("city_plans", planId,
                    com.medievalfactions.domain.MFCityPlanner.CityPlan.class);
            if (plan.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Plan urbano inexistente.")); return 0; }
            for (var building : plan.get().buildings()) ctx.getSource().sendSuccess(() -> Component.literal(
                    building.id() + " | " + building.blueprintId() + " | " + building.stage() + " | progreso "
                            + building.progress() + "/" + building.requiredProgress()), false);
            return plan.get().buildings().size();
        })));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> constructionCommands() {
        var command = Commands.literal("construction");
        command.then(Commands.literal("build").requires(source -> source.hasPermission(2))
                .then(Commands.argument("settlement_id", StringArgumentType.word())
                .then(Commands.argument("blueprint", StringArgumentType.word())
                .then(Commands.argument("rotation", IntegerArgumentType.integer(0, 3)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BlockPos origin = player.blockPosition().relative(player.getDirection(), 3);
                    UUID settlement = uuid(ctx, "settlement_id");
                    var project = MFAdvancedSavedData.get(ctx.getSource().getServer()).startConstruction(
                            MFWorldSavedData.get(ctx.getSource().getServer()), settlement, player.getUUID(),
                            player.serverLevel().dimension().location().toString(), origin.getX(), origin.getY(), origin.getZ(),
                            IntegerArgumentType.getInteger(ctx, "rotation"), StringArgumentType.getString(ctx, "blueprint"),
                            MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
                    if (project.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Construcción rechazada: revisa materiales, tesorería, plano y ubicación.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Proyecto financiado: " + project.get().id() + " | se colocarán bloques automáticamente en lotes pequeños. Libera la zona si queda bloqueado."), true);
                    return 1;
                })))));
        command.then(Commands.literal("list").requires(source -> source.hasPermission(2)).executes(ctx -> {
            var projects = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("construction", ConstructionProject.class);
            for (ConstructionProject p : projects) ctx.getSource().sendSuccess(() -> Component.literal(p.id() + " | " + p.blueprintId() + " | " + p.status() + " | bloque " + p.nextPlacement()), false);
            return projects.size();
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> companionCommands() {
        var command = Commands.literal("companion");
        // Debug/admin spawn only; in the finished survival loop, rare companions will be obtained through gameplay.
        command.then(Commands.literal("summon").requires(source -> source.hasPermission(2))
                .then(Commands.argument("species", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            CompanionSpecies species = parseEnum(CompanionSpecies.class, StringArgumentType.getString(ctx, "species"));
            EntityType<?> type = species == null ? null : switch (species) {
                case HORSE -> MFEntities.HORSE.get();
                case GIANT_WOLF -> MFEntities.WOLF.get();
                case GRIFFIN -> MFEntities.GRIFFIN.get();
                case PHOENIX -> MFEntities.PHOENIX.get();
                case DRAGON -> MFEntities.DRAGON.get();
                default -> null;
            };
            if (type == null) {
                ctx.getSource().sendFailure(Component.literal("Especie no disponible como entidad todavía. Disponibles ahora: HORSE, GIANT_WOLF, GRIFFIN, PHOENIX, DRAGON."));
                return 0;
            }
            Entity created = type.create(player.serverLevel());
            if (!(created instanceof MFAnimatedMob mount)) {
                ctx.getSource().sendFailure(Component.literal("No se pudo crear la montura.")); return 0;
            }
            mount.setMountOwner(player.getUUID());
            mount.moveTo(player.getX() + 1.5D, player.getY(), player.getZ(), player.getYRot(), 0.0F);
            if (!player.serverLevel().addFreshEntity(mount)) {
                ctx.getSource().sendFailure(Component.literal("El mundo rechazó la entidad.")); return 0;
            }
            int maxStamina = species == CompanionSpecies.DRAGON ? 100 : 60;
            var companion = MFAdvancedSavedData.get(ctx.getSource().getServer()).registerCompanion(
                    mount.getUUID(), player.getUUID(), species, Math.max(1, (int) mount.getMaxHealth()), maxStamina);
            if (companion.isEmpty()) { mount.discard(); ctx.getSource().sendFailure(Component.literal("No se pudo registrar el vínculo persistente.")); return 0; }
            boolean riding = player.startRiding(mount, true);
            ctx.getSource().sendSuccess(() -> Component.literal("Montura invocada: " + species + " | UUID " + mount.getUUID()
                    + " | vínculo " + companion.get().bondLevel() + (riding ? " | ya estás montado" : " | acércate para montar")), false);
            return 1;
        })));
        command.then(Commands.literal("feed").then(Commands.argument("entity_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID id = uuid(ctx, "entity_id");
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var companion = data.record("companions", id, Companion.class);
            if (companion.isEmpty() || !companion.get().ownerId().equals(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("No tienes esa montura registrada.")); return 0;
            }
            if (companion.get().hunger() == 0) { ctx.getSource().sendFailure(Component.literal("Esta montura no necesita comer ahora.")); return 0; }
            if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getFoodProperties(player) == null) {
                ctx.getSource().sendFailure(Component.literal("Sostén comida en la mano principal.")); return 0;
            }
            var updated = companion.get().feed(25);
            if (updated == companion.get()) return 0;
            player.getMainHandItem().shrink(1);
            data.updateCompanion(updated);
            ctx.getSource().sendSuccess(() -> Component.literal("Alimentación aplicada. Hambre: " + updated.hunger() + "/100."), false);
            return 1;
        })));
        var genomeCommand = Commands.literal("genome").requires(source -> source.hasPermission(2));
        var genomeTemperament = Commands.argument("temperament", IntegerArgumentType.integer(1, 100)).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID dragonId = uuid(ctx, "dragon_id");
            Element element = parseEnum(Element.class, StringArgumentType.getString(ctx, "element"));
            var genome = new com.medievalfactions.domain.MFDragonLifecycle.DragonGenome(element,
                    IntegerArgumentType.getInteger(ctx, "vitality"), IntegerArgumentType.getInteger(ctx, "agility"),
                    IntegerArgumentType.getInteger(ctx, "flame"), IntegerArgumentType.getInteger(ctx, "intelligence"),
                    IntegerArgumentType.getInteger(ctx, "temperament"));
            boolean saved = MFAdvancedSavedData.get(ctx.getSource().getServer()).registerDragonGenome(dragonId, player.getUUID(), genome);
            if (!saved) { ctx.getSource().sendFailure(Component.literal("Genoma rechazado: el dragón debe existir, estar vivo y pertenecer al jugador.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Genoma persistido para " + dragonId + " | elemento " + element), true);
            return 1;
        });
        var genomeIntelligence = Commands.argument("intelligence", IntegerArgumentType.integer(1, 100)).then(genomeTemperament);
        var genomeFlame = Commands.argument("flame", IntegerArgumentType.integer(1, 100)).then(genomeIntelligence);
        var genomeAgility = Commands.argument("agility", IntegerArgumentType.integer(1, 100)).then(genomeFlame);
        var genomeVitality = Commands.argument("vitality", IntegerArgumentType.integer(1, 100)).then(genomeAgility);
        var genomeElement = Commands.argument("element", StringArgumentType.word()).then(genomeVitality);
        genomeCommand.then(Commands.argument("dragon_id", StringArgumentType.word()).then(genomeElement));
        command.then(genomeCommand);
        command.then(Commands.literal("breed").requires(source -> source.hasPermission(2))
                .then(Commands.argument("parent_a", StringArgumentType.word())
                .then(Commands.argument("parent_b", StringArgumentType.word())
                .then(Commands.argument("seed", LongArgumentType.longArg()).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    var egg = MFAdvancedSavedData.get(ctx.getSource().getServer()).breedDragons(player.getUUID(),
                            uuid(ctx, "parent_a"), uuid(ctx, "parent_b"), LongArgumentType.getLong(ctx, "seed"),
                            (int) Math.min(Integer.MAX_VALUE, Math.max(0L,
                                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays())));
                    if (egg.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Cría rechazada: se necesitan dos dragones adultos, vivos, del mismo propietario y con genomas registrados.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Huevo genético creado: " + egg.get().id() + " | herencia " + egg.get().genome()), true);
                    return 1;
                })))));
        command.then(Commands.literal("incubate").requires(source -> source.hasPermission(2))
                .then(Commands.argument("egg_id", StringArgumentType.word())
                .then(Commands.argument("warmth", IntegerArgumentType.integer(0, 100))
                .then(Commands.argument("care", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
                    UUID eggId = uuid(ctx, "egg_id");
                    var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
                    var egg = data.record("dragon_eggs", eggId, com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class).orElse(null);
                    if (egg == null || !egg.ownerId().equals(ctx.getSource().getPlayerOrException().getUUID())) {
                        ctx.getSource().sendFailure(Component.literal("No tienes ese huevo.")); return 0;
                    }
                    var updated = data.updateDragonEggEnvironment(eggId, IntegerArgumentType.getInteger(ctx, "warmth"),
                            IntegerArgumentType.getInteger(ctx, "care"));
                    ctx.getSource().sendSuccess(() -> Component.literal("Incubación actualizada: " + updated.map(e -> e.incubationProgress() + "/" + e.requiredProgress()).orElse("sin cambios")), true);
                    return updated.isPresent() ? 1 : 0;
                })))));
        var hatchCommand = Commands.literal("hatch").requires(source -> source.hasPermission(2));
        hatchCommand.then(Commands.argument("egg_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID eggId = uuid(ctx, "egg_id");
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var egg = data.record("dragon_eggs", eggId, com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class).orElse(null);
            if (egg == null || !egg.ownerId().equals(player.getUUID())
                    || egg.status() != com.medievalfactions.domain.MFDragonLifecycle.EggStatus.READY_TO_HATCH) {
                ctx.getSource().sendFailure(Component.literal("El huevo no te pertenece o todavía no está listo.")); return 0;
            }
            var level = player.serverLevel();
            MFDragonEntity dragon = MFEntities.DRAGON.get().create(level);
            if (dragon == null) return 0;
            var hatchlingId = UUID.randomUUID();
            var hatchling = com.medievalfactions.domain.MFDragonLifecycle.createHatchling(egg, hatchlingId).orElse(null);
            if (hatchling == null) return 0;
            var maxHealth = Math.max(1, hatchling.maxHealth());
            if (dragon.getAttribute(Attributes.MAX_HEALTH) != null) dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
            dragon.setHealth(maxHealth);
            dragon.setMountOwner(player.getUUID());
            dragon.setPersistenceRequired();
            dragon.moveTo(player.getX()+1.5D, player.getY(), player.getZ(), player.getYRot(), 0.0F);
            if (!level.addFreshEntity(dragon)) { dragon.discard(); return 0; }
            var persisted = data.hatchDragonIntoCompanion(eggId, hatchlingId, dragon.getUUID(), player.getUUID());
            if (persisted.isEmpty()) { dragon.discard(); ctx.getSource().sendFailure(Component.literal("No se pudo guardar la eclosión y el vínculo del dragón.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("¡Eclosión! Dragón joven creado: " + dragon.getUUID() + " | genética heredada; la escala visual juvenil aún es provisional."), true);
            return 1;
        }));
        command.then(hatchCommand);
        command.then(Commands.literal("dragon").then(Commands.literal("breath").executes(ctx -> dragonBreath(ctx.getSource().getPlayerOrException()))));
        command.then(Commands.literal("attack").executes(ctx -> companionAttack(ctx.getSource().getPlayerOrException())));
        command.then(Commands.literal("list").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var list = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("companions", Companion.class).stream()
                    .filter(c -> c.ownerId().equals(player.getUUID())).toList();
            for (Companion c : list) ctx.getSource().sendSuccess(() -> Component.literal(c.id() + " | " + c.species() + " | nivel " + c.level()
                    + " | vínculo " + c.bondLevel() + " | hambre " + c.hunger() + " | resistencia " + c.stamina()
                    + " | " + (c.alive() ? "vivo" : "muerto")), false);
            return list.size();
        }));
        return command;
    }

    private static int companionAttack(ServerPlayer player) {
        if (!(player.getVehicle() instanceof MFAnimatedMob mount) || !player.getUUID().equals(mount.getMountOwner())) {
            player.sendSystemMessage(Component.literal("Debes estar montado en tu propia montura.")); return 0;
        }
        var data = MFAdvancedSavedData.get(player.serverLevel().getServer());
        Companion companion = data.record("companions", mount.getUUID(), Companion.class).orElse(null);
        if (companion == null || !companion.canRide(player.getUUID()) || companion.stamina() < 8) {
            player.sendSystemMessage(Component.literal("La montura necesita alimento y al menos 8 puntos de resistencia.")); return 0;
        }
        if (companion.species() == CompanionSpecies.DRAGON) return dragonBreath(player);
        if (companion.species() != CompanionSpecies.GIANT_WOLF && companion.species() != CompanionSpecies.GRIFFIN
                && companion.species() != CompanionSpecies.PHOENIX) {
            player.sendSystemMessage(Component.literal("Esta especie no tiene todavía una acción ofensiva propia.")); return 0;
        }
        LivingEntity target = aimedLivingTarget(player, 4.5D, candidate -> candidate != mount);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Apunta a un enemigo cercano.")); return 0;
        }
        float damage;
        if (mount instanceof MFWolfEntity wolf) { wolf.triggerSecondaryAction(); damage = 10.0F; }
        else if (mount instanceof MFGriffinEntity griffin) { griffin.triggerPrimaryAction(); damage = 12.0F; }
        else if (mount instanceof MFPhoenixEntity phoenix) {
            phoenix.triggerPrimaryAction();
            target.igniteForSeconds(4.0F);
            phoenix.playSound(MFSounds.MAGIC_FIRE_IMPACT.get(), 0.9F, 1.0F);
            damage = 9.0F;
        }
        else return 0;
        boolean applied = target.hurt(mount.damageSources().mobAttack(mount), damage);
        Companion updated = companion.ride(80).train(2L);
        data.updateCompanion(updated);
        player.sendSystemMessage(Component.literal("Ataque de montura " + companion.species() + (applied ? " acertado" : " bloqueado")
                + " | resistencia " + updated.stamina() + " | hambre " + updated.hunger()));
        return applied ? 1 : 0;
    }

    private static int dragonBreath(ServerPlayer player) {
        if (!(player.getVehicle() instanceof MFDragonEntity dragon) || !player.getUUID().equals(dragon.getMountOwner())) {
            player.sendSystemMessage(Component.literal("Debes montar tu propio dragón para usar su aliento.")); return 0;
        }
        var data = MFAdvancedSavedData.get(player.serverLevel().getServer());
        var companion = data.record("companions", dragon.getUUID(), Companion.class).orElse(null);
        if (companion == null || !companion.dragon() || !companion.canRide(player.getUUID()) || companion.stamina() < 10) {
            player.sendSystemMessage(Component.literal("El dragón necesita vínculo, alimento y al menos 10 puntos de resistencia.")); return 0;
        }
        dragon.playBreathAnimation();
        dragon.playSound(MFSounds.CREATURE_DRAGON_BREATH_RELEASE.get(), 1.0F, 0.96F);
        Vec3 look = player.getLookAngle().normalize();
        AABB area = dragon.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target != dragon && target.isAlive()
                        && target.position().subtract(dragon.position()).lengthSqr() <= 64.0D
                        && target.position().subtract(dragon.position()).normalize().dot(look) >= 0.55D);
        int hit = 0;
        for (LivingEntity target : targets.stream().limit(6).toList()) {
            target.igniteForSeconds(6.0F);
            if (target.hurt(dragon.damageSources().mobAttack(dragon), 12.0F)) hit++;
        }
        Companion next = companion.ride(100).train(2L);
        data.updateCompanion(next);
        player.sendSystemMessage(Component.literal("Aliento de dragón: " + hit + " objetivos alcanzados | resistencia " + next.stamina() + " | hambre " + next.hunger()));
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> familyCommands() {
        var command = Commands.literal("family");

        var registerName = Commands.argument("family_name", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var profile = MFWorldSavedData.get(ctx.getSource().getServer()).profile(player.getUUID());
            if (profile.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Primero crea tu personaje MEDIEVAL FACTIONS.")); return 0; }
            Sex sex = parseEnum(Sex.class, StringArgumentType.getString(ctx, "sex"));
            if (sex == null) { ctx.getSource().sendFailure(Component.literal("Sexo biológico inválido: FEMALE o MALE.")); return 0; }
            var faction = MFFactionSavedData.get(ctx.getSource().getServer()).factionForPlayer(player.getUUID());
            if (faction.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Debes pertenecer a una facción para registrar un linaje.")); return 0; }
            var p = profile.get();
            Person person = new Person(player.getUUID(), faction.get().id(), p.race(), p.affinity(), p.element(),
                    sex, MFRaceBiology.adultAgeDays(p.race()), 100, StringArgumentType.getString(ctx, "family_name"));
            boolean ok = MFAdvancedSavedData.get(ctx.getSource().getServer()).registerPerson(person);
            if (!ok) { ctx.getSource().sendFailure(Component.literal("La persona ya está registrada.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Persona registrada para linaje: " + person.id() + " | " + person.familyName()), false);
            return 1;
        });
        var registerSex = Commands.argument("sex", StringArgumentType.word()).then(registerName);
        command.then(Commands.literal("register").then(registerSex));

        var relationConsentB = Commands.argument("consent_b", BoolArgumentType.bool()).executes(ctx -> {
            var relation = MFAdvancedSavedData.get(ctx.getSource().getServer()).createRelationship(
                    uuid(ctx, "person_a"), uuid(ctx, "person_b"), IntegerArgumentType.getInteger(ctx, "trust"),
                    BoolArgumentType.getBool(ctx, "consent_a"), BoolArgumentType.getBool(ctx, "consent_b"),
                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
            if (relation.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Relación rechazada: ambas personas deben ser adultas, compatibles y consentir explícitamente.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Relación creada: " + relation.get().id()), true);
            return 1;
        });
        var relationConsentA = Commands.argument("consent_a", BoolArgumentType.bool()).then(relationConsentB);
        var relationTrust = Commands.argument("trust", IntegerArgumentType.integer(0, 100)).then(relationConsentA);
        var relationB = Commands.argument("person_b", StringArgumentType.word()).then(relationTrust);
        var relationA = Commands.argument("person_a", StringArgumentType.word()).then(relationB);
        var relationCommand = Commands.literal("relation").requires(source -> source.hasPermission(2)).then(relationA);
        command.then(relationCommand);

        var marriageProposer = Commands.argument("proposer", StringArgumentType.word()).executes(ctx -> {
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            Person a = data.record("persons", uuid(ctx, "person_a"), Person.class).orElse(null);
            Person b = data.record("persons", uuid(ctx, "person_b"), Person.class).orElse(null);
            var marriage = data.proposeMarriage(a, b, uuid(ctx, "proposer"), MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
            if (marriage.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Propuesta rechazada: ambas personas deben ser adultas, compatibles y tener relación consentida de cortejo.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Propuesta de matrimonio creada: " + marriage.get().id() + " | falta el consentimiento de la otra persona."), true);
            return 1;
        });
        var marriageB = Commands.argument("person_b", StringArgumentType.word()).then(marriageProposer);
        var marriageA = Commands.argument("person_a", StringArgumentType.word()).then(marriageB);
        var proposeCommand = Commands.literal("propose").then(marriageA);
        var answerConsent = Commands.argument("consent", BoolArgumentType.bool()).executes(ctx -> {
            var answer = MFAdvancedSavedData.get(ctx.getSource().getServer()).answerMarriage(uuid(ctx, "marriage_id"),
                    uuid(ctx, "respondent"), BoolArgumentType.getBool(ctx, "consent"),
                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());
            if (answer.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Respuesta inválida o propuesta vencida.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Respuesta registrada: " + answer.get().status()), true);
            return 1;
        });
        var answerRespondent = Commands.argument("respondent", StringArgumentType.word()).then(answerConsent);
        var answerMarriageId = Commands.argument("marriage_id", StringArgumentType.word()).then(answerRespondent);
        var answerCommand = Commands.literal("answer").then(answerMarriageId);
        var divorceCommand = Commands.literal("divorce").then(Commands.argument("marriage_id", StringArgumentType.word()).executes(ctx -> {
            var result = MFAdvancedSavedData.get(ctx.getSource().getServer()).dissolveMarriage(uuid(ctx, "marriage_id"));
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No existe un matrimonio activo con ese identificador.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Matrimonio disuelto; la relación queda cerrada."), true);
            return 1;
        }));
        var marriageCommand = Commands.literal("marriage").requires(source -> source.hasPermission(2));
        marriageCommand.then(proposeCommand); marriageCommand.then(answerCommand); marriageCommand.then(divorceCommand);
        command.then(marriageCommand);

        var clinicBeds = Commands.argument("beds", IntegerArgumentType.integer(1, 1000)).executes(ctx -> {
            UUID settlementId = uuid(ctx, "settlement_id");
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            boolean saved = MFAdvancedSavedData.get(ctx.getSource().getServer()).registerClinic(world, settlementId,
                    IntegerArgumentType.getInteger(ctx, "tier"), IntegerArgumentType.getInteger(ctx, "beds"));
            if (!saved) { ctx.getSource().sendFailure(Component.literal("No se pudo registrar: el asentamiento necesita un médico ciudadano activo y un almacén válido.")); return 0; }
            long stock = world.settlement(settlementId).map(st -> st.inventory().getOrDefault("medieval_factions:healing_potion", 0L)).orElse(0L);
            ctx.getSource().sendSuccess(() -> Component.literal("Clínica registrada. Medicinas reales en almacén: " + stock + " | camas: "
                    + IntegerArgumentType.getInteger(ctx, "beds")), true);
            return 1;
        });
        var clinicTier = Commands.argument("tier", IntegerArgumentType.integer(1, 5)).then(clinicBeds);
        var clinicSettlement = Commands.argument("settlement_id", StringArgumentType.word()).then(clinicTier);
        var clinicRegister = Commands.literal("register").then(clinicSettlement);

        var caseInfection = Commands.argument("infection", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
            UUID patientId = uuid(ctx, "patient_id");
            boolean exists = MFWorldSavedData.get(ctx.getSource().getServer()).citizens(null).stream()
                    .anyMatch(c -> c.id().equals(patientId) && c.status() == CitizenStatus.ACTIVE);
            var condition = parseEnum(com.medievalfactions.domain.MFFamilyCare.Condition.class, StringArgumentType.getString(ctx, "condition"));
            if (!exists || condition == null) { ctx.getSource().sendFailure(Component.literal("Paciente o condición no válidos.")); return 0; }
            UUID caseId = UUID.randomUUID();
            var medical = new com.medievalfactions.domain.MFFamilyCare.MedicalCase(caseId, patientId, condition,
                    IntegerArgumentType.getInteger(ctx, "severity"), IntegerArgumentType.getInteger(ctx, "infection"),
                    com.medievalfactions.domain.MFFamilyCare.MedicalStatus.OPEN,
                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays(), -1);
            boolean stored = MFAdvancedSavedData.get(ctx.getSource().getServer()).storeSimulationRecord("medical_cases", caseId, medical,
                    com.medievalfactions.domain.MFFamilyCare.MedicalCase.class);
            if (!stored) return 0;
            ctx.getSource().sendSuccess(() -> Component.literal("Caso médico abierto: " + caseId), true);
            return 1;
        });
        var caseSeverity = Commands.argument("severity", IntegerArgumentType.integer(0, 100)).then(caseInfection);
        var caseCondition = Commands.argument("condition", StringArgumentType.word()).then(caseSeverity);
        var casePatient = Commands.argument("patient_id", StringArgumentType.word()).then(caseCondition);
        var clinicOpen = Commands.literal("open").then(casePatient);

        var treatClinic = Commands.argument("clinic_settlement_id", StringArgumentType.word()).executes(ctx -> {
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var treated = MFAdvancedSavedData.get(ctx.getSource().getServer()).treatMedicalCase(world,
                    uuid(ctx, "case_id"), uuid(ctx, "clinic_settlement_id"), world.simulatedDays());
            if (treated.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Tratamiento rechazado: faltan cama, médico, medicina física, paciente del asentamiento o disponibilidad diaria.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Tratamiento aplicado: salud +" + treated.get().healthRestored()
                    + " | medicinas restantes " + treated.get().clinic().medicineStock()), true);
            return 1;
        });
        var treatCase = Commands.argument("case_id", StringArgumentType.word()).then(treatClinic);
        var clinicTreat = Commands.literal("treat").then(treatCase);
        var clinicCommand = Commands.literal("clinic").requires(source -> source.hasPermission(2));
        clinicCommand.then(clinicRegister); clinicCommand.then(clinicOpen); clinicCommand.then(clinicTreat);
        command.then(clinicCommand);

        var conceiveSeed = Commands.argument("seed", LongArgumentType.longArg()).executes(ctx -> {
            var pregnancy = MFAdvancedSavedData.get(ctx.getSource().getServer()).conceive(uuid(ctx, "relationship_id"),
                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays(), IntegerArgumentType.getInteger(ctx, "fertility_roll"),
                    LongArgumentType.getLong(ctx, "seed"));
            if (pregnancy.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No hay concepción: revisa consentimiento, edad, salud, relación, compatibilidad y fertilidad.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Embarazo registrado. Gestación estimada: " + pregnancy.get().gestationDays() + " días."), true);
            return 1;
        });
        var conceiveRoll = Commands.argument("fertility_roll", IntegerArgumentType.integer(0, 99)).then(conceiveSeed);
        var conceiveRelation = Commands.argument("relationship_id", StringArgumentType.word()).then(conceiveRoll);
        command.then(Commands.literal("conceive").requires(source -> source.hasPermission(2)).then(conceiveRelation));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> expeditionCommands() {
        var command = Commands.literal("expedition").requires(source -> source.hasPermission(2));
        var membersArg = Commands.argument("member_ids_csv", StringArgumentType.greedyString()).executes(ctx -> {
            List<UUID> members = new ArrayList<>();
            for (String value : StringArgumentType.getString(ctx, "member_ids_csv").split(",")) {
                UUID id = parseUuid(value.trim()); if (id != null) members.add(id);
            }
            var expedition = MFAdvancedSavedData.get(ctx.getSource().getServer()).startExpedition(
                    uuid(ctx, "faction_id"), uuid(ctx, "settlement_id"), members, LongArgumentType.getLong(ctx, "seed"),
                    IntegerArgumentType.getInteger(ctx, "tier"), IntegerArgumentType.getInteger(ctx, "days"),
                    LongArgumentType.getLong(ctx, "provisions"), IntegerArgumentType.getInteger(ctx, "party_power"),
                    MFWorldSavedData.get(ctx.getSource().getServer()));
            if (expedition.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Expedición rechazada: se necesitan 1-8 miembros y pan suficiente en el almacén.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Expedición enviada: " + expedition.get().id() + " | nivel " + expedition.get().dungeonTier() + " | vuelve en " + expedition.get().daysRemaining() + " días."), true);
            return 1;
        });
        var seedArg = Commands.argument("seed", LongArgumentType.longArg()).then(membersArg);
        var powerArg = Commands.argument("party_power", IntegerArgumentType.integer(1, 1_000_000)).then(seedArg);
        var provisionsArg = Commands.argument("provisions", LongArgumentType.longArg(1)).then(powerArg);
        var daysArg = Commands.argument("days", IntegerArgumentType.integer(1, 365)).then(provisionsArg);
        var tierArg = Commands.argument("tier", IntegerArgumentType.integer(1, 10)).then(daysArg);
        var settlementArg = Commands.argument("settlement_id", StringArgumentType.word()).then(tierArg);
        var factionArg = Commands.argument("faction_id", StringArgumentType.word()).then(settlementArg);
        command.then(Commands.literal("send").then(factionArg));
        command.then(Commands.literal("list").executes(ctx -> {
            var list = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("expeditions", Expedition.class);
            for (Expedition e : list) ctx.getSource().sendSuccess(() -> Component.literal(e.id() + " | " + e.status() + " | nivel " + e.dungeonTier() + " | " + e.daysRemaining() + " días | provisiones " + e.provisions()), false);
            return list.size();
        }));
        command.then(Commands.literal("route").then(Commands.argument("expedition_id", StringArgumentType.word())
                .then(Commands.argument("destination", BlockPosArgument.blockPos()).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    var advanced = MFAdvancedSavedData.get(ctx.getSource().getServer());
                    var world = MFWorldSavedData.get(ctx.getSource().getServer());
                    UUID expeditionId = uuid(ctx, "expedition_id");
                    Expedition expedition = advanced.record("expeditions", expeditionId, Expedition.class).orElse(null);
                    if (expedition == null) { ctx.getSource().sendFailure(Component.literal("Expedición inexistente.")); return 0; }
                    long sumX = 0, sumY = 0, sumZ = 0;
                    for (UUID memberId : expedition.memberIds()) {
                        MFCitizenSimulation.ResidentEntityLink link = advanced.record("resident_links", memberId,
                                MFCitizenSimulation.ResidentEntityLink.class).orElse(null);
                        if (link == null || !link.dimension().equals(player.serverLevel().dimension().location().toString())) {
                            ctx.getSource().sendFailure(Component.literal("Todos los miembros deben tener un NPC materializado en la dimensión actual. Usa /mf advanced npc spawn <citizen_uuid>."));
                            return 0;
                        }
                        Entity entity = player.serverLevel().getEntity(link.entityId());
                        if (!(entity instanceof MFResidentEntity resident) || !memberId.equals(resident.citizenId())) {
                            ctx.getSource().sendFailure(Component.literal("No está cargado el NPC de uno de los miembros; acércate a ellos y vuelve a intentarlo."));
                            return 0;
                        }
                        BlockPos pos = resident.blockPosition();
                        sumX += pos.getX(); sumY += pos.getY(); sumZ += pos.getZ();
                    }
                    BlockPos target = BlockPosArgument.getLoadedBlockPos(ctx, "destination");
                    int startX = (int)Math.floor((double)sumX / expedition.memberIds().size());
                    int startY = (int)Math.floor((double)sumY / expedition.memberIds().size());
                    int startZ = (int)Math.floor((double)sumZ / expedition.memberIds().size());
                    String dimension = player.serverLevel().dimension().location().toString();
                    var route = List.of(new com.medievalfactions.domain.MFAdventureSystems.RoutePoint(dimension, startX, startY, startZ),
                            new com.medievalfactions.domain.MFAdventureSystems.RoutePoint(dimension, target.getX(), target.getY(), target.getZ()));
                    var journey = new com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney(UUID.randomUUID(), expedition.id(),
                            expedition.memberIds(), route, 0, expedition.provisions(),
                            com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.OUTBOUND, 0, 0, -1);
                    var started = advanced.startPhysicalExpedition(world, journey);
                    if (started.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se pudo activar la ruta: revisa miembros, provisiones, trabajos y dimensión.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Expedición física iniciada: los " + expedition.memberIds().size()
                            + " NPC seguirán la ruta hacia " + target.toShortString() + ". El viaje consume provisiones cada día."), true);
                    return 1;
                }))));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> dungeonCommands() {
        var command = Commands.literal("dungeon").requires(source -> source.hasPermission(2));
        command.then(Commands.literal("generate").then(Commands.argument("seed", LongArgumentType.longArg())
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10)).executes(ctx -> {
                    var layout = MFAdvancedSystems.generateDungeon(LongArgumentType.getLong(ctx, "seed"), IntegerArgumentType.getInteger(ctx, "tier"));
                    ctx.getSource().sendSuccess(() -> Component.literal("Plano determinista: " + layout.rooms().size() + " salas | poder " + layout.totalEncounterPower() + " | jefe " + layout.bossId()), false);
                    return layout.rooms().size();
                }))));
        command.then(Commands.literal("build").then(Commands.argument("seed", LongArgumentType.longArg())
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BlockPos origin = player.blockPosition().offset(8, -6, 0);
                    var project = MFAdvancedSavedData.get(ctx.getSource().getServer()).startDungeonBuild(
                            LongArgumentType.getLong(ctx, "seed"), IntegerArgumentType.getInteger(ctx, "tier"),
                            player.serverLevel().dimension().location().toString(), origin.getX(), origin.getY(), origin.getZ());
                    if (project.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se pudo encolar: máximo tres mazmorras en construcción.")); return 0; }
                    ctx.getSource().sendSuccess(() -> Component.literal("Generación física encolada: " + project.get().id()
                            + " | comprueba que la cueva/zona esté vacía; nunca se sobrescribe terreno sólido."), true);
                    return 1;
                }))));
        command.then(Commands.literal("list").executes(ctx -> {
            var projects = MFAdvancedSavedData.get(ctx.getSource().getServer()).records("dungeons", DungeonProject.class);
            for (DungeonProject p : projects) ctx.getSource().sendSuccess(() -> Component.literal(p.id() + " | nivel " + p.tier() + " | " + p.status() + " | bloque " + p.nextPlacement()), false);
            return projects.size();
        }));
        return command;
    }

    /** Operator-only preview for all registered SoundEvents. */
    private static LiteralArgumentBuilder<CommandSourceStack> soundCommands() {
        var command = Commands.literal("sound").requires(source -> source.hasPermission(2));
        command.then(Commands.literal("preview")
                .then(Commands.argument("sound_id", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(MFSoundPreview.ids(), builder))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(ctx, "sound_id").toLowerCase(Locale.ROOT);
                            var sound = MFSoundPreview.find(id);
                            if (sound == null) {
                                ctx.getSource().sendFailure(Component.literal("Sonido no registrado: " + id));
                                return 0;
                            }
                            player.level().playSound(null, player.blockPosition(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            ctx.getSource().sendSuccess(() -> Component.literal("Reproduciendo " + id), false);
                            return 1;
                        })));
        return command;
    }

    /**
     * Server-side entity ray trace that respects blocks between the caster and target.
     * ServerPlayer.pick() only reports block hits, so it must not be used to select entities.
     */
    private static LivingEntity aimedLivingTarget(ServerPlayer player, double range,
                                                    java.util.function.Predicate<LivingEntity> extraFilter) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 fullEnd = start.add(direction.scale(range));
        HitResult blockHit = player.pick(range, 0.0F, false);
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? fullEnd : blockHit.getLocation();
        AABB search = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0D);

        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search,
                entity -> entity != player && entity.isAlive() && entity.isPickable() && extraFilter.test(entity))) {
            var intercept = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(start, end);
            if (intercept.isEmpty()) continue;
            double distance = start.distanceToSqr(intercept.get());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = candidate;
            }
        }
        return closest;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> skillCommands() {
        var command = Commands.literal("skill");
        command.then(Commands.literal("list").executes(ctx -> {
            for (AbilitySpec a : MFAdvancedSystems.starterAbilities()) ctx.getSource().sendSuccess(() -> Component.literal(a.id() + " | clase " + a.requiredClass() + " | elemento " + a.requiredElement() + " | coste " + a.cost() + " " + a.resource() + " | poder " + a.power() + " | enfriamiento " + a.cooldownTicks() + " ticks"), false);
            ctx.getSource().sendSuccess(() -> Component.literal("elemental_burst | cualquier clase | elemento propio | coste 25 mana | daño 8-16 | enfriamiento 160 ticks"), false);
            ctx.getSource().sendSuccess(() -> Component.literal("shadow_blink | cualquier clase | coste 28 mana | salto seguro de hasta 7 bloques | enfriamiento 240 ticks"), false);
            return MFAdvancedSystems.starterAbilities().size() + 2;
        }));
        command.then(Commands.literal("cast").then(Commands.argument("ability_id", StringArgumentType.word()).executes(ctx -> castSkill(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "ability_id")))));
        return command;
    }

    private static int castSkill(ServerPlayer player, String abilityId) {
        var profile = MFWorldSavedData.get(player.serverLevel().getServer()).profile(player.getUUID());
        if (profile.isEmpty()) { player.sendSystemMessage(Component.literal("Primero crea tu personaje MEDIEVAL FACTIONS.")); return 0; }
        if (abilityId.equalsIgnoreCase("elemental_burst")) return castElementalBurst(player, profile.get());
        if (abilityId.equalsIgnoreCase("shadow_blink")) return castShadowBlink(player);
        AbilitySpec ability = MFAdvancedSystems.starterAbilities().stream().filter(a -> a.id().equalsIgnoreCase(abilityId)).findFirst().orElse(null);
        if (ability == null) { player.sendSystemMessage(Component.literal("Habilidad desconocida. Usa /mf advanced skill list.")); return 0; }
        CompoundTag data = player.getPersistentData();
        int mana = data.contains("mf_mana", Tag.TAG_INT) ? data.getInt("mf_mana") : 100;
        int stamina = data.contains("mf_stamina", Tag.TAG_INT) ? data.getInt("mf_stamina") : 100;
        int cooldown = data.getInt("mf_cd_" + ability.id());
        Map<String, Integer> cooldowns = cooldown > 0 ? java.util.Map.of(ability.id(), cooldown) : java.util.Map.of();
        int health = Math.max(1, (int) Math.ceil(player.getHealth()));
        int maxHealth = Math.max(1, (int) Math.ceil(player.getMaxHealth()));
        Combatant caster = new Combatant(player.getUUID(), profile.get().mainClass(), profile.get().element(), health, maxHealth,
                Math.max(0, mana), 100, Math.max(0, stamina), 100, cooldowns);
        LivingEntity livingTarget = null;
        Combatant target = null;
        if (ability.effect() != AbilityEffect.HEAL) {
            livingTarget = aimedLivingTarget(player, 16.0D, candidate -> true);
            if (livingTarget == null) {
                player.sendSystemMessage(Component.literal("Apunta a una criatura viva dentro del alcance.")); return 0;
            }
            int targetHp = Math.max(1, (int) Math.ceil(livingTarget.getHealth()));
            int targetMax = Math.max(targetHp, (int) Math.ceil(livingTarget.getMaxHealth()));
            target = new Combatant(livingTarget.getUUID(), CombatClass.WARRIOR, null, targetHp, targetMax, 0, 0, 0, 1, java.util.Map.of());
        }
        var cast = MFAdvancedSystems.cast(caster, target, ability);
        if (cast.isEmpty()) { player.sendSystemMessage(Component.literal("No puedes usar esta habilidad: clase, elemento, recurso o enfriamiento no válidos.")); return 0; }
        int effect = cast.get().actualEffect();
        switch (ability.effect()) {
            case HEAL -> player.heal(effect);
            case DAMAGE -> livingTarget.hurt(player.damageSources().playerAttack(player), effect);
            case FIRE_DAMAGE -> { livingTarget.igniteForSeconds(4.0F); livingTarget.hurt(player.damageSources().magic(), effect); }
            case ROOT -> livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Math.max(40, ability.cooldownTicks()), 2));
        }
        var castSound = switch (ability.effect()) {
            case HEAL -> MFSounds.MAGIC_HOLY_BURST.get();
            case DAMAGE -> MFSounds.MAGIC_ARCANE_RELEASE.get();
            case FIRE_DAMAGE -> elementalImpactSound(ability.requiredElement() != null
                    ? ability.requiredElement() : profile.get().element());
            case ROOT -> MFSounds.MAGIC_NATURE_IMPACT.get();
        };
        player.playNotifySound(castSound, SoundSource.PLAYERS, 0.9F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        data.putInt("mf_mana", cast.get().caster().mana());
        data.putInt("mf_stamina", cast.get().caster().stamina());
        data.putInt("mf_cd_" + ability.id(), ability.cooldownTicks());
        player.sendSystemMessage(Component.literal("Habilidad ejecutada: " + ability.id() + " | efecto " + effect + " | mana " + cast.get().caster().mana() + " | resistencia " + cast.get().caster().stamina()));
        return 1;
    }

    /** Element-specific burst available to every created character; effects resolve on the server. */
    private static int castElementalBurst(ServerPlayer player, com.medievalfactions.domain.MFCharacterProfile profile) {
        var data = player.getPersistentData();
        int mana = data.contains("mf_mana", Tag.TAG_INT) ? data.getInt("mf_mana") : 100;
        int cooldown = data.getInt("mf_cd_elemental_burst");
        if (mana < 25 || cooldown > 0) {
            player.sendSystemMessage(Component.literal("Elemental Burst no disponible: requiere 25 de mana y no tener enfriamiento."));
            return 0;
        }
        LivingEntity target = aimedLivingTarget(player, 16.0D, candidate -> true);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Apunta a una criatura viva dentro de 16 bloques."));
            return 0;
        }
        int damage = 8 + Math.min(8, profile.classLevel() / 8);
        if (!target.hurt(player.damageSources().magic(), damage)) {
            player.sendSystemMessage(Component.literal("El objetivo no recibe daño de este elemento."));
            return 0;
        }
        data.putInt("mf_mana", mana - 25);
        data.putInt("mf_cd_elemental_burst", 160);
        player.playNotifySound(MFSounds.MAGIC_ARCANE_CHARGE.get(), SoundSource.PLAYERS, 0.78F, 1.0F);
        player.serverLevel().playSound(null, target.blockPosition(), elementalImpactSound(profile.element()),
                SoundSource.HOSTILE, 0.9F, 0.94F + player.getRandom().nextFloat() * 0.12F);
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + target.getBbHeight() * 0.55D,
                target.getZ(), 14, 0.32D, 0.42D, 0.32D, 0.12D);
        player.sendSystemMessage(Component.literal("Explosión elemental " + profile.element() + ": " + damage + " de daño. Mana restante: " + (mana - 25)));
        return 1;
    }

    /** Safe short-range shadow relocation; the server checks collision and support before moving the player. */
    private static int castShadowBlink(ServerPlayer player) {
        var data = player.getPersistentData();
        int mana = data.contains("mf_mana", Tag.TAG_INT) ? data.getInt("mf_mana") : 100;
        int cooldown = data.getInt("mf_cd_shadow_blink");
        if (mana < 28 || cooldown > 0) {
            player.sendSystemMessage(Component.literal("Shadow Blink no disponible: requiere 28 de mana y no tener enfriamiento."));
            return 0;
        }
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 0.001D) return 0;
        forward = forward.normalize();
        Vec3 origin = player.position();
        Vec3 destination = null;
        for (int distance = 7; distance >= 2 && destination == null; distance--) {
            Vec3 candidateBase = origin.add(forward.scale(distance));
            BlockPos base = BlockPos.containing(candidateBase.x, origin.y, candidateBase.z);
            for (int yOffset = 1; yOffset >= -1; yOffset--) {
                Vec3 candidate = new Vec3(candidateBase.x, base.getY() + yOffset, candidateBase.z);
                BlockPos feet = BlockPos.containing(candidate.x, candidate.y, candidate.z);
                BlockPos supportPos = feet.below();
                if (!player.serverLevel().hasChunkAt(supportPos)) continue;
                var support = player.serverLevel().getBlockState(supportPos);
                if (!support.isFaceSturdy(player.serverLevel(), supportPos, Direction.UP)) continue;
                if (!player.serverLevel().getFluidState(feet).isEmpty()) continue;
                var movedBox = player.getBoundingBox().move(candidate.subtract(origin));
                if (!player.serverLevel().containsAnyLiquid(movedBox)
                        && player.serverLevel().noCollision(player, movedBox)) {
                    destination = candidate;
                    break;
                }
            }
        }
        if (destination == null) {
            player.sendSystemMessage(Component.literal("No hay un punto seguro para Shadow Blink en esa dirección."));
            return 0;
        }
        Vec3 departure = player.position();
        player.teleportTo(destination.x, destination.y, destination.z);
        data.putInt("mf_mana", mana - 28);
        data.putInt("mf_cd_shadow_blink", 240);
        player.playNotifySound(MFSounds.MAGIC_SHADOW_BLINK.get(), SoundSource.PLAYERS, 0.95F, 1.0F);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, departure.x, departure.y + 0.8D, departure.z,
                18, 0.3D, 0.5D, 0.3D, 0.25D);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, destination.x, destination.y + 0.8D, destination.z,
                18, 0.3D, 0.5D, 0.3D, 0.25D);
        player.sendSystemMessage(Component.literal("Shadow Blink: desplazamiento seguro de " + String.format(Locale.ROOT, "%.1f", departure.distanceTo(destination)) + " bloques."));
        return 1;
    }

    private static net.minecraft.sounds.SoundEvent elementalImpactSound(com.medievalfactions.domain.MFGameCatalog.Element element) {
        return switch (element) {
            case FIRE -> MFSounds.MAGIC_FIRE_IMPACT.get();
            case LIGHT -> MFSounds.MAGIC_LIGHT_IMPACT.get();
            case LIGHTNING -> MFSounds.MAGIC_LIGHTNING_IMPACT.get();
            case EARTH -> MFSounds.MAGIC_EARTH_IMPACT.get();
            case METAL -> MFSounds.MAGIC_METAL_IMPACT.get();
            case AETHER -> MFSounds.MAGIC_ETHER_IMPACT.get();
            case WATER -> MFSounds.MAGIC_WATER_IMPACT.get();
            case ICE -> MFSounds.MAGIC_ICE_IMPACT.get();
            case AIR -> MFSounds.MAGIC_AIR_IMPACT.get();
            case DARKNESS -> MFSounds.MAGIC_DARKNESS_IMPACT.get();
            case NATURE -> MFSounds.MAGIC_NATURE_IMPACT.get();
            case SPIRIT -> MFSounds.MAGIC_SPIRIT_IMPACT.get();
        };
    }

    /** Lightweight in-game administrative overview; detailed screens can replace this text panel later. */
    private static LiteralArgumentBuilder<CommandSourceStack> dashboardCommand() {
        return Commands.literal("dashboard").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var advanced = MFAdvancedSavedData.get(ctx.getSource().getServer());
            var faction = MFFactionSavedData.get(ctx.getSource().getServer()).factionForPlayer(player.getUUID());
            ctx.getSource().sendSystemMessage(Component.literal("§6§lMEDIEVAL FACTIONS — PANEL DE MANDO"));
            ctx.getSource().sendSystemMessage(Component.literal("§eCalendario: §f" + world.simulatedDays() + " días | ciudadanos: " + world.citizens(null).size()));
            ctx.getSource().sendSystemMessage(Component.literal("§eFacción: §f" + faction.map(f -> f.name() + " | " + f.playerMembers().size() + "/2 jugadores").orElse("sin facción")));
            ctx.getSource().sendSystemMessage(Component.literal("§eIA NPC: §f" + advanced.count("npc_directives") + " | caravanas: " + advanced.count("caravans") + " | expediciones: " + advanced.count("expeditions")));
            ctx.getSource().sendSystemMessage(Component.literal("§eBancos: §f" + advanced.count("bank_accounts") + " cuentas | préstamos: " + advanced.count("loans") + " | asedios: " + advanced.count("sieges")));
            ctx.getSource().sendSystemMessage(Component.literal("§eFamilia: §f" + advanced.count("persons") + " personas | " + advanced.count("pregnancies") + " gestaciones | linajes: " + advanced.count("lineage")));
            ctx.getSource().sendSystemMessage(Component.literal("§eMundo: §f" + advanced.count("construction") + " construcciones | " + advanced.count("dungeons") + " mazmorras | " + advanced.count("companions") + " monturas/dragones"));
            ctx.getSource().sendSystemMessage(Component.literal("§eCiudad y guerra: §f" + advanced.count("city_plans") + " planes | " + advanced.count("city_build_links") + " obras físicas urbanas | " + advanced.count("tactical_battles") + " batallas | " + advanced.count("army_columns") + " columnas"));
            ctx.getSource().sendSystemMessage(Component.literal("§eVida y aventura: §f" + advanced.count("dragon_eggs") + " huevos | " + advanced.count("dragon_genomes") + " genomas | " + advanced.count("marriages") + " matrimonios | " + advanced.count("medical_cases") + " casos médicos | " + advanced.count("clinics") + " clínicas"));
            ctx.getSource().sendSystemMessage(Component.literal("§eExpediciones físicas: §f" + advanced.count("expedition_journeys") + " rutas | caravanas físicas: " + advanced.count("physical_convoys") + " | jefes: " + advanced.count("boss_encounters")));
            ctx.getSource().sendSystemMessage(Component.literal("§7Panel rápido disponible (O); skins con sincronización de servidor integrada (pendiente prueba con dos clientes). Siguen pendientes la GUI de gestión completa, IA social/laboral autónoma y entidades físicas de ejército y caravanas."));
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> statusCommand() {
        return Commands.literal("status").requires(source -> source.hasPermission(2)).executes(ctx -> {
            var data = MFAdvancedSavedData.get(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.literal("Sistemas avanzados persistentes | NPC " + data.count("npc_directives")
                    + " | caravanas " + data.count("caravans") + " | cuentas " + data.count("bank_accounts")
                    + " | préstamos " + data.count("loans") + " | asedios " + data.count("sieges")
                    + " | compañeros " + data.count("companions") + " | relaciones " + data.count("relationships")
                    + " | embarazos " + data.count("pregnancies") + " | expediciones " + data.count("expeditions")
                    + " | construcciones " + data.count("construction") + " | mazmorras " + data.count("dungeons")
                    + " | horarios " + data.count("citizen_schedules") + " | ciudades " + data.count("city_plans")
                    + " | batallas " + data.count("tactical_battles") + " | huevos " + data.count("dragon_eggs")
                    + " | matrimonios " + data.count("marriages") + " | casos médicos " + data.count("medical_cases")
                    + " | rutas físicas " + data.count("physical_convoys") + " | jefes " + data.count("boss_encounters")), false);
            return 1;
        });
    }

    private static UUID uuid(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String argument) {
        return parseUuid(StringArgumentType.getString(ctx, argument));
    }
    private static UUID parseUuid(String raw) {
        try { return UUID.fromString(raw); } catch (RuntimeException ex) { return null; }
    }
}
