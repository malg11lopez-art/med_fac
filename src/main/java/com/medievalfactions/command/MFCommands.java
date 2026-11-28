package com.medievalfactions.command;

import com.medievalfactions.data.MFFactionSavedData;
import com.medievalfactions.data.MFWorldSavedData;
import com.medievalfactions.domain.MFCalendar;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFTradeEngine;
import com.medievalfactions.domain.MFSkillTreeCatalog;
import com.medievalfactions.domain.MFRecipeCatalog;
import com.medievalfactions.registry.MFEntities;
import com.medievalfactions.registry.MFSounds;
import net.minecraft.sounds.SoundSource;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.Locale;
import java.util.UUID;

import static com.medievalfactions.domain.MFGameCatalog.*;

@EventBusSubscriber(modid = "medieval_factions", bus = EventBusSubscriber.Bus.GAME)
public final class MFCommands {
    private MFCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mf");
        root.then(spawnCommand());
        root.then(factionCommand());
        root.then(characterCommand());
        root.then(calendarCommand());
        root.then(settlementCommand());
        root.then(diplomacyCommand());
        root.then(MFAdvancedCommands.command());
        event.getDispatcher().register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> spawnCommand() {
        var command = Commands.literal("spawn").requires(source -> source.hasPermission(2));
        command.then(Commands.argument("kind", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String kind = StringArgumentType.getString(ctx, "kind").toLowerCase(Locale.ROOT);
            EntityType<?> type = switch (kind) {
                case "guard" -> MFEntities.GUARD.get(); case "horse" -> MFEntities.HORSE.get();
                case "dragon" -> MFEntities.DRAGON.get(); case "centaur" -> MFEntities.CENTAUR.get();
                case "harpy" -> MFEntities.HARPY.get(); case "merfolk" -> MFEntities.MERFOLK.get();
                case "draconid" -> MFEntities.DRACONID.get(); case "wolf" -> MFEntities.WOLF.get();
                case "griffin" -> MFEntities.GRIFFIN.get(); case "phoenix" -> MFEntities.PHOENIX.get();
                default -> null;
            };
            if (type == null) {
                ctx.getSource().sendFailure(Component.literal("Tipo desconocido: guard, horse, dragon, centaur, harpy, merfolk, draconid, wolf, griffin o phoenix."));
                return 0;
            }
            Entity entity = type.create(player.serverLevel());
            if (entity == null) return 0;
            entity.moveTo(player.getX() + 2.0D, player.getY(), player.getZ(), player.getYRot(), 0.0F);
            boolean added = player.serverLevel().addFreshEntity(entity);
            if (added) ctx.getSource().sendSuccess(() -> Component.literal("Entidad de prueba creada: " + kind), true);
            else ctx.getSource().sendFailure(Component.literal("No se pudo crear la entidad."));
            return added ? 1 : 0;
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> factionCommand() {
        var command = Commands.literal("faction");
        var adminName = Commands.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
            RaceFamily race = MFGameCatalog.parseEnum(RaceFamily.class, StringArgumentType.getString(ctx, "race"));
            if (race == null) { ctx.getSource().sendFailure(Component.literal("Familia racial principal no válida.")); return 0; }
            var created = MFFactionSavedData.get(ctx.getSource().getServer()).createFaction(
                    StringArgumentType.getString(ctx, "name"), null, race);
            if (created.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Nombre inválido o duplicado.")); return 0; }
            var f = created.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Facción creada: " + f.name() + " | Raza principal: " + f.primaryRace() + " | ID: " + f.id()), true);
            return 1;
        });
        command.then(Commands.literal("create").requires(source -> source.hasPermission(2))
                .then(Commands.argument("race", StringArgumentType.word()).then(adminName)));

        var foundName = Commands.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            RaceFamily race = MFGameCatalog.parseEnum(RaceFamily.class, StringArgumentType.getString(ctx, "race"));
            if (race == null) { ctx.getSource().sendFailure(Component.literal("Raza principal no válida.")); return 0; }
            var created = MFFactionSavedData.get(ctx.getSource().getServer()).createFaction(
                    StringArgumentType.getString(ctx, "name"), player.getUUID(), race);
            if (created.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se pudo fundar: ya perteneces a una facción o el nombre está ocupado.")); return 0; }
            var f = created.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Facción fundada: " + f.name() + " | Raza principal: " + f.primaryRace() + " | ID: " + f.id() + " | Plazas de jugadores: 1/2"), true);
            return 1;
        });
        command.then(Commands.literal("found").then(Commands.argument("race", StringArgumentType.word()).then(foundName)));

        var joinId = Commands.argument("faction_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID id = parseUuid(StringArgumentType.getString(ctx, "faction_id"));
            boolean ok = id != null && MFFactionSavedData.get(ctx.getSource().getServer()).joinFaction(id, player.getUUID());
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Te has unido a la facción."), false);
            else ctx.getSource().sendFailure(Component.literal("No se pudo entrar: facción inexistente, ya estás en otra o la facción ya tiene dos jugadores."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("join").then(joinId));
        command.then(Commands.literal("leave").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            boolean ok = MFFactionSavedData.get(ctx.getSource().getServer()).leaveFaction(player.getUUID());
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Has abandonado tu facción."), false);
            else ctx.getSource().sendFailure(Component.literal("No perteneces a ninguna facción."));
            return ok ? 1 : 0;
        }));
        command.then(Commands.literal("list").executes(ctx -> {
            var factions = MFFactionSavedData.get(ctx.getSource().getServer()).allFactions();
            ctx.getSource().sendSuccess(() -> Component.literal("Facciones registradas: " + factions.size()), false);
            factions.values().forEach(f -> ctx.getSource().sendSuccess(() -> Component.literal("- " + f.name() + " | jugadores " + f.playerMembers().size() + "/2 | raza principal " + (f.primaryRace() == null ? "sin definir" : f.primaryRace()) + " | tesorería " + f.treasuryCopper() + " cobres | ID " + f.id()), false));
            return factions.size();
        }));
        command.then(Commands.literal("info").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var faction = MFFactionSavedData.get(ctx.getSource().getServer()).factionForPlayer(player.getUUID());
            if (faction.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No perteneces a ninguna facción.")); return 0; }
            var f = faction.get();
            ctx.getSource().sendSuccess(() -> Component.literal(f.name() + " | miembros jugadores " + f.playerMembers().size() + "/2 | raza principal " + f.primaryRace() + " | cobre " + f.treasuryCopper()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("ID: " + f.id() + " | líder: " + (f.leaderPlayerId() == null ? "sin líder" : f.leaderPlayerId())), false);
            return 1;
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> characterCommand() {
        var command = Commands.literal("character");
        var profession = Commands.argument("profession", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            RaceFamily race = MFGameCatalog.parseEnum(RaceFamily.class, StringArgumentType.getString(ctx, "race"));
            CombatClass combatClass = MFGameCatalog.parseEnum(CombatClass.class, StringArgumentType.getString(ctx, "class"));
            Profession professionValue = MFGameCatalog.parseEnum(Profession.class, StringArgumentType.getString(ctx, "profession"));
            if (race == null || combatClass == null || professionValue == null) {
                ctx.getSource().sendFailure(Component.literal("Raza, clase o profesión no reconocida. Usa los identificadores en inglés del catálogo."));
                return 0;
            }
            long seed = player.getUUID().getMostSignificantBits() ^ player.serverLevel().getDayTime() ^ player.getUUID().getLeastSignificantBits();
            var created = MFWorldSavedData.get(ctx.getSource().getServer()).createProfile(player.getUUID(), race, combatClass, professionValue, seed);
            if (created.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Ya tienes un personaje. La afinidad y el elemento no se pueden volver a tirar."));
                return 0;
            }
            var p = created.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Personaje creado: " + p.race() + " | " + p.mainClass() + " | " + p.mainProfession() + " | Afinidad " + p.affinity() + " | Elemento " + p.element()), false);
            return 1;
        });
        var classArg = Commands.argument("class", StringArgumentType.word()).then(profession);
        var raceArg = Commands.argument("race", StringArgumentType.word()).then(classArg);
        command.then(Commands.literal("create").then(raceArg));
        command.then(Commands.literal("info").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var profile = MFWorldSavedData.get(ctx.getSource().getServer()).profile(player.getUUID());
            if (profile.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Aún no tienes personaje. Usa /mf character create <race> <class> <profession>."));
                return 0;
            }
            var p = profile.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Raza: " + p.race() + " | Afinidad: " + p.affinity() + " | Elemento: " + p.element()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Clase: " + p.mainClass() + " nivel " + p.classLevel() + " | Subclase: " + (p.subclass() == null ? "ninguna" : p.subclass())), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Profesión: " + p.mainProfession() + " nivel " + p.professionLevel() + " | Talentos: " + p.talentPointsAvailable() + "/" + p.talentPointsEarned()), false);
            return 1;
        }));
        command.then(Commands.literal("tree").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var profile = MFWorldSavedData.get(ctx.getSource().getServer()).profile(player.getUUID());
            if (profile.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Primero crea tu personaje.")); return 0; }
            var p = profile.get();
            var nodes = MFSkillTreeCatalog.visibleTree(p.mainClass());
            ctx.getSource().sendSuccess(() -> Component.literal("Árbol " + p.mainClass() + " | " + nodes.size() + " nodos | Talentos disponibles " + p.talentPointsAvailable()), false);
            for (var node : nodes) {
                if (node.kind() == MFSkillTreeCatalog.NodeKind.CORE || p.unlockedNodes().contains(node.id())
                        || p.classLevel() >= node.minimumClassLevel()) {
                    String state = p.unlockedNodes().contains(node.id()) ? "[DESBLOQUEADO]" : "[nivel " + node.minimumClassLevel() + "]";
                    ctx.getSource().sendSuccess(() -> Component.literal(state + " " + node.id() + " — " + node.displayName()), false);
                }
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Subclases: " + MFSkillTreeCatalog.branches(p.mainClass()).stream().map(MFSkillTreeCatalog.Branch::subclassId).toList()), false);
            return 1;
        }));
        var nodeUnlock = Commands.argument("node_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var profile = world.profile(player.getUUID());
            if (profile.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Primero crea tu personaje.")); return 0; }
            try {
                var updated = profile.get().unlockNode(StringArgumentType.getString(ctx, "node_id"));
                world.updateProfile(updated);
                player.playNotifySound(MFSounds.UI_SKILL_UNLOCK.get(), SoundSource.PLAYERS, 0.72F, 1.0F);
                ctx.getSource().sendSuccess(() -> Component.literal("Talento desbloqueado. Quedan " + updated.talentPointsAvailable() + " puntos."), false);
                return 1;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ctx.getSource().sendFailure(Component.literal("No se puede desbloquear: " + ex.getMessage()));
                return 0;
            }
        });
        command.then(Commands.literal("talent").then(Commands.literal("unlock").then(nodeUnlock)));
        var chooseSubclass = Commands.argument("subclass_id", StringArgumentType.word()).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var profile = world.profile(player.getUUID());
            if (profile.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Primero crea tu personaje.")); return 0; }
            try {
                var updated = profile.get().chooseSubclass(StringArgumentType.getString(ctx, "subclass_id"));
                world.updateProfile(updated);
                ctx.getSource().sendSuccess(() -> Component.literal("Subclase elegida de forma permanente: " + updated.subclass()), false);
                return 1;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ctx.getSource().sendFailure(Component.literal("No se puede elegir esa subclase: " + ex.getMessage()));
                return 0;
            }
        });
        command.then(Commands.literal("subclass").then(Commands.literal("choose").then(chooseSubclass)));
        command.then(Commands.literal("trial").requires(source -> source.hasPermission(2)).then(Commands.literal("complete").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var profile = world.profile(player.getUUID());
            if (profile.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No hay personaje registrado.")); return 0; }
            world.updateProfile(profile.get().completeSubclassTrial());
            ctx.getSource().sendSuccess(() -> Component.literal("Prueba marcada como superada (herramienta administrativa de pruebas)."), true);
            return 1;
        })));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> calendarCommand() {
        return Commands.literal("calendar").executes(ctx -> {
            MFCalendar.Date date = MFCalendar.fromMinecraftDayTime(ctx.getSource().getServer().overworld().getDayTime());
            ctx.getSource().sendSuccess(() -> Component.literal("Año " + date.year() + " | " + date.weekday() + " | " + date.dayOfMonth() + " de " + date.month() + " | Estación " + date.season()), false);
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> settlementCommand() {
        var command = Commands.literal("settlement").requires(source -> source.hasPermission(2));
        var createName = Commands.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
            UUID factionId = parseUuid(StringArgumentType.getString(ctx, "faction_id"));
            if (factionId == null || MFFactionSavedData.get(ctx.getSource().getServer()).getFaction(factionId).isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("ID de facción incorrecto o inexistente.")); return 0;
            }
            var created = MFWorldSavedData.get(ctx.getSource().getServer()).createSettlement(factionId, StringArgumentType.getString(ctx, "name"));
            if (created.isEmpty()) { ctx.getSource().sendFailure(Component.literal("No se pudo fundar: nombre inválido o repetido.")); return 0; }
            var s = created.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Campamento fundado: " + s.name() + " | ID: " + s.id() + " | Ayuntamiento nivel 1."), true);
            return 1;
        });
        command.then(Commands.literal("create").then(Commands.argument("faction_id", StringArgumentType.word()).then(createName)));
        command.then(Commands.literal("list").executes(ctx -> {
            var list = MFWorldSavedData.get(ctx.getSource().getServer()).settlements();
            ctx.getSource().sendSuccess(() -> Component.literal("Asentamientos: " + list.size()), false);
            for (var s : list) ctx.getSource().sendSuccess(() -> Component.literal(s.name() + " | " + s.tier() + " | población " + s.population() + "/" + s.housingCapacity() + " | comida " + s.foodRations() + " | ID " + s.id()), false);
            return list.size();
        }));

        var infoId = Commands.argument("settlement_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            var settlement = id == null ? java.util.Optional.<com.medievalfactions.domain.MFSettlementState>empty()
                    : MFWorldSavedData.get(ctx.getSource().getServer()).settlement(id);
            if (settlement.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Asentamiento inexistente.")); return 0; }
            var value = settlement.get();
            ctx.getSource().sendSuccess(() -> Component.literal(value.name() + " | " + value.tier() + " | ID " + value.id()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Población " + value.population() + "/" + value.housingCapacity()
                    + " | Ayuntamiento " + value.townHallLevel() + " | comida " + value.foodRations() + " (" + value.foodDays() + " días)"), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Tesorería " + value.treasuryCopper() + " cobre | mantenimiento "
                    + value.dailyMaintenanceCopper() + "/día | seguridad " + value.security() + " | infraestructura " + value.infrastructure()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Almacén: " + value.inventory()), false);
            return 1;
        });
        command.then(Commands.literal("info").then(infoId));

        var upgradeId = Commands.argument("settlement_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            boolean upgraded = id != null && world.upgradeSettlement(id);
            if (!upgraded) { ctx.getSource().sendFailure(Component.literal("No se puede ascender: revisa población, Ayuntamiento, vivienda, seguridad y reservas de comida; además solo hay una Capital por facción.")); return 0; }
            var value = world.settlement(id).orElseThrow();
            ctx.getSource().sendSuccess(() -> Component.literal("Asentamiento ascendido a " + value.tier() + "."), true);
            return 1;
        });
        command.then(Commands.literal("upgrade").then(upgradeId));

        var projectInfrastructure = Commands.argument("infrastructure", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            boolean applied = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).applySettlementProject(
                    id, java.util.Map.of(), LongArgumentType.getLong(ctx, "cost_copper"),
                    IntegerArgumentType.getInteger(ctx, "housing"), IntegerArgumentType.getInteger(ctx, "hall"),
                    IntegerArgumentType.getInteger(ctx, "security"), IntegerArgumentType.getInteger(ctx, "infrastructure"));
            if (applied) ctx.getSource().sendSuccess(() -> Component.literal("Proyecto financiado y aplicado. Cobre descontado de la tesorería."), true);
            else ctx.getSource().sendFailure(Component.literal("Proyecto rechazado: revisa saldo, mejoras y límites del Ayuntamiento."));
            return applied ? 1 : 0;
        });
        var projectSecurity = Commands.argument("security", IntegerArgumentType.integer(0, 100)).then(projectInfrastructure);
        var projectHall = Commands.argument("hall", IntegerArgumentType.integer(0, 4)).then(projectSecurity);
        var projectHousing = Commands.argument("housing", IntegerArgumentType.integer(0, 10_000)).then(projectHall);
        var projectCost = Commands.argument("cost_copper", LongArgumentType.longArg(1L)).then(projectHousing);
        command.then(Commands.literal("project").then(Commands.argument("settlement_id", StringArgumentType.word()).then(projectCost)));

        var addCitizenPotential = Commands.argument("potential", IntegerArgumentType.integer(1, 50)).executes(ctx -> {
            UUID settlementId = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            var world = MFWorldSavedData.get(ctx.getSource().getServer());
            var settlement = settlementId == null ? java.util.Optional.<com.medievalfactions.domain.MFSettlementState>empty() : world.settlement(settlementId);
            RaceFamily race = MFGameCatalog.parseEnum(RaceFamily.class, StringArgumentType.getString(ctx, "race"));
            Profession profession = MFGameCatalog.parseEnum(Profession.class, StringArgumentType.getString(ctx, "profession"));
            var faction = settlement.isEmpty() ? java.util.Optional.<MFFactionSavedData.FactionRecord>empty()
                    : MFFactionSavedData.get(ctx.getSource().getServer()).getFaction(settlement.get().factionId());
            if (settlement.isEmpty() || faction.isEmpty() || race == null || profession == null) { ctx.getSource().sendFailure(Component.literal("Asentamiento, facción, raza o profesión no válidos.")); return 0; }
            if (faction.get().primaryRace() != null && race != faction.get().primaryRace()) {
                ctx.getSource().sendFailure(Component.literal("La incorporación permanente exige la familia racial principal de la facción: " + faction.get().primaryRace()));
                return 0;
            }
            var citizen = MFCitizenState.create(settlement.get().factionId(), settlementId,
                    StringArgumentType.getString(ctx, "name"), race, profession,
                    LongArgumentType.getLong(ctx, "wage_copper"), IntegerArgumentType.getInteger(ctx, "potential"), MFRaceBiology.adultAgeDays(race));
            if (!world.addCitizen(citizen, faction.get().primaryRace())) { ctx.getSource().sendFailure(Component.literal("No se pudo añadir: comprueba capacidad de vivienda, nombre, facción y familia racial.")); return 0; }
            ctx.getSource().sendSuccess(() -> Component.literal("Habitante añadido: " + citizen.name() + " | " + citizen.race() + " | " + citizen.profession() + " | ID: " + citizen.id()), true);
            return 1;
        });
        var wage = Commands.argument("wage_copper", LongArgumentType.longArg(0L)).then(addCitizenPotential);
        var prof = Commands.argument("profession", StringArgumentType.word()).then(wage);
        var race = Commands.argument("race", StringArgumentType.word()).then(prof);
        var citizenName = Commands.argument("name", StringArgumentType.word()).then(race);
        command.then(Commands.literal("citizen").then(Commands.literal("add").then(Commands.argument("settlement_id", StringArgumentType.word()).then(citizenName))));

        var foodAmount = Commands.argument("rations", LongArgumentType.longArg(1L)).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            boolean ok = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).addSettlementFood(id, LongArgumentType.getLong(ctx, "rations"));
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Raciones añadidas al almacén."), true);
            else ctx.getSource().sendFailure(Component.literal("No se pudo añadir comida."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("food").then(Commands.literal("add").then(Commands.argument("settlement_id", StringArgumentType.word()).then(foodAmount))));

        var treasuryAmount = Commands.argument("copper", LongArgumentType.longArg(1L)).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            boolean ok = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).adjustSettlementTreasury(id, LongArgumentType.getLong(ctx, "copper"));
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Tesorería actualizada."), true);
            else ctx.getSource().sendFailure(Component.literal("No se pudo añadir saldo."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("treasury").then(Commands.literal("add").then(Commands.argument("settlement_id", StringArgumentType.word()).then(treasuryAmount))));

        var maintenanceAmount = Commands.argument("copper_per_day", LongArgumentType.longArg(0L)).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            boolean ok = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).setSettlementDailyMaintenance(
                    id, LongArgumentType.getLong(ctx, "copper_per_day"));
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Mantenimiento diario configurado desde la tesorería del asentamiento."), true);
            else ctx.getSource().sendFailure(Component.literal("No se pudo configurar el mantenimiento."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("maintenance").then(Commands.literal("set")
                .then(Commands.argument("settlement_id", StringArgumentType.word()).then(maintenanceAmount))));

        var stockAmount = Commands.argument("amount", LongArgumentType.longArg(1L)).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            boolean ok = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).addSettlementStock(id,
                    StringArgumentType.getString(ctx, "item_id"), LongArgumentType.getLong(ctx, "amount"));
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Existencias añadidas al almacén."), true);
            else ctx.getSource().sendFailure(Component.literal("ID de objeto, cantidad o asentamiento no válidos."));
            return ok ? 1 : 0;
        });
        var itemId = Commands.argument("item_id", StringArgumentType.word()).then(stockAmount);
        command.then(Commands.literal("stock").then(Commands.literal("add").then(Commands.argument("settlement_id", StringArgumentType.word()).then(itemId))));

        var tradePrice = Commands.argument("unit_price_copper", LongArgumentType.longArg(0L)).executes(ctx -> {
            UUID seller = parseUuid(StringArgumentType.getString(ctx, "seller_id"));
            UUID buyer = parseUuid(StringArgumentType.getString(ctx, "buyer_id"));
            java.util.Optional<MFTradeEngine.TradeResult> result = seller == null || buyer == null ? java.util.Optional.empty()
                    : MFWorldSavedData.get(ctx.getSource().getServer()).trade(seller, buyer,
                    StringArgumentType.getString(ctx, "item_id"), LongArgumentType.getLong(ctx, "quantity"),
                    LongArgumentType.getLong(ctx, "unit_price_copper"));
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Comercio rechazado: faltan bienes o fondos, ID inválido o desbordamiento.")); return 0; }
            var t = result.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Comercio completado: " + t.quantity() + " x " + t.itemId() + " por " + t.totalPriceCopper() + " cobres."), true);
            return 1;
        });
        var tradeQuantity = Commands.argument("quantity", LongArgumentType.longArg(1L)).then(tradePrice);
        var tradeItem = Commands.argument("item_id", StringArgumentType.word()).then(tradeQuantity);
        var tradeBuyer = Commands.argument("buyer_id", StringArgumentType.word()).then(tradeItem);
        command.then(Commands.literal("trade").then(Commands.argument("seller_id", StringArgumentType.word()).then(tradeBuyer)));

        var citizenList = Commands.argument("settlement_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            if (id == null || MFWorldSavedData.get(ctx.getSource().getServer()).settlement(id).isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Asentamiento inexistente.")); return 0;
            }
            var citizens = MFWorldSavedData.get(ctx.getSource().getServer()).citizens(id);
            ctx.getSource().sendSuccess(() -> Component.literal("Habitantes registrados: " + citizens.size()), false);
            for (var citizen : citizens) ctx.getSource().sendSuccess(() -> Component.literal(citizen.name() + " | " + citizen.profession() + " nivel " + citizen.professionLevel() + " | " + citizen.status() + " | ID " + citizen.id()), false);
            return citizens.size();
        });
        command.then(Commands.literal("citizen").then(Commands.literal("list").then(citizenList)));

        var workStartRecipe = Commands.argument("recipe_id", StringArgumentType.word()).executes(ctx -> {
            UUID settlementId = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            UUID workerId = parseUuid(StringArgumentType.getString(ctx, "citizen_id"));
            var result = settlementId == null || workerId == null ? java.util.Optional.<com.medievalfactions.domain.MFWorkOrderState>empty()
                    : MFWorldSavedData.get(ctx.getSource().getServer()).startWorkOrder(settlementId, workerId, StringArgumentType.getString(ctx, "recipe_id"));
            if (result.isEmpty()) { ctx.getSource().sendFailure(Component.literal("Trabajo rechazado: revisa profesión, trabajador, materiales y si ya tiene un encargo.")); return 0; }
            var order = result.get();
            ctx.getSource().sendSuccess(() -> Component.literal("Encargo iniciado: " + order.recipeId() + " | ID " + order.id() + " | los materiales ya están reservados."), true);
            return 1;
        });
        var workWorker = Commands.argument("citizen_id", StringArgumentType.word()).then(workStartRecipe);
        var workSettlement = Commands.argument("settlement_id", StringArgumentType.word()).then(workWorker);
        command.then(Commands.literal("work").then(Commands.literal("start").then(workSettlement)));
        var workListId = Commands.argument("settlement_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "settlement_id"));
            if (id == null) { ctx.getSource().sendFailure(Component.literal("ID inválido.")); return 0; }
            var orders = MFWorldSavedData.get(ctx.getSource().getServer()).workOrders(id);
            ctx.getSource().sendSuccess(() -> Component.literal("Encargos: " + orders.size()), false);
            for (var order : orders) ctx.getSource().sendSuccess(() -> Component.literal(order.recipeId() + " | " + order.status() + " | días " + order.elapsedDays() + " | trabajador " + order.workerId() + " | ID " + order.id()), false);
            return orders.size();
        });
        command.then(Commands.literal("work").then(Commands.literal("list").then(workListId)));
        var cancelOrderId = Commands.argument("order_id", StringArgumentType.word()).executes(ctx -> {
            UUID id = parseUuid(StringArgumentType.getString(ctx, "order_id"));
            boolean ok = id != null && MFWorldSavedData.get(ctx.getSource().getServer()).cancelUnstartedWorkOrder(id);
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Encargo cancelado y materiales devueltos."), true);
            else ctx.getSource().sendFailure(Component.literal("No se puede cancelar: solo se reembolsa antes del primer día de trabajo."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("work").then(Commands.literal("cancel").then(cancelOrderId)));

        command.then(Commands.literal("recipes").executes(ctx -> {
            ctx.getSource().sendSuccess(() -> Component.literal("Recetas registradas: " + MFRecipeCatalog.all().size()), false);
            for (var recipe : MFRecipeCatalog.all()) ctx.getSource().sendSuccess(() -> Component.literal(recipe.id() + " | profesión " + recipe.profession() + " | días " + recipe.durationDays() + " | entradas " + recipe.inputs() + " | salidas " + recipe.outputs()), false);
            return MFRecipeCatalog.all().size();
        }));

        command.then(Commands.literal("simulate-day").executes(ctx -> {
            MFWorldSavedData.get(ctx.getSource().getServer()).advanceOneDay();
            ctx.getSource().sendSuccess(() -> Component.literal("Simulación diaria ejecutada."), true);
            return 1;
        }));
        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> diplomacyCommand() {
        var command = Commands.literal("diplomacy").requires(source -> source.hasPermission(2));
        var relationDelta = Commands.argument("delta", IntegerArgumentType.integer(-200, 200)).executes(ctx -> {
            UUID a = parseUuid(StringArgumentType.getString(ctx, "faction_a"));
            UUID b = parseUuid(StringArgumentType.getString(ctx, "faction_b"));
            boolean ok = a != null && b != null && MFWorldSavedData.get(ctx.getSource().getServer()).adjustRelation(a, b, IntegerArgumentType.getInteger(ctx, "delta"));
            if (ok) {
                var r = MFWorldSavedData.get(ctx.getSource().getServer()).relation(a, b).orElseThrow();
                ctx.getSource().sendSuccess(() -> Component.literal("Relación: " + r.score() + " (" + r.band() + ")"), true);
            } else ctx.getSource().sendFailure(Component.literal("Facciones inválidas o iguales."));
            return ok ? 1 : 0;
        });
        var relationB = Commands.argument("faction_b", StringArgumentType.word()).then(relationDelta);
        var relationA = Commands.argument("faction_a", StringArgumentType.word()).then(relationB);
        command.then(Commands.literal("relation").then(relationA));

        var warB = Commands.argument("faction_b", StringArgumentType.word()).executes(ctx -> {
            UUID a = parseUuid(StringArgumentType.getString(ctx, "faction_a"));
            UUID b = parseUuid(StringArgumentType.getString(ctx, "faction_b"));
            boolean ok = a != null && b != null && MFWorldSavedData.get(ctx.getSource().getServer()).declareWar(a, b);
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Declaración de guerra registrada."), true);
            else ctx.getSource().sendFailure(Component.literal("Facciones inválidas o iguales."));
            return ok ? 1 : 0;
        });
        command.then(Commands.literal("war").then(Commands.argument("faction_a", StringArgumentType.word()).then(warB)));

        var peaceScore = Commands.argument("post_war_score", IntegerArgumentType.integer(-100, 100)).executes(ctx -> {
            UUID a = parseUuid(StringArgumentType.getString(ctx, "faction_a"));
            UUID b = parseUuid(StringArgumentType.getString(ctx, "faction_b"));
            boolean ok = a != null && b != null && MFWorldSavedData.get(ctx.getSource().getServer()).makePeace(a, b, IntegerArgumentType.getInteger(ctx, "post_war_score"));
            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Paz registrada."), true);
            else ctx.getSource().sendFailure(Component.literal("Facciones inválidas o iguales."));
            return ok ? 1 : 0;
        });
        var peaceB = Commands.argument("faction_b", StringArgumentType.word()).then(peaceScore);
        command.then(Commands.literal("peace").then(Commands.argument("faction_a", StringArgumentType.word()).then(peaceB)));
        return command;
    }

    private static UUID parseUuid(String raw) {
        try { return UUID.fromString(raw); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
