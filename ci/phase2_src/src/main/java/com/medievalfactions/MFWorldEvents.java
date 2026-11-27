package com.medievalfactions;

import com.medievalfactions.data.MFWorldSavedData;
import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.data.MFFactionSavedData;
import com.medievalfactions.entity.MFAnimatedMob;
import com.medievalfactions.entity.MFResidentEntity;
import com.medievalfactions.domain.MFAdvancedSystems.Companion;
import com.medievalfactions.domain.MFServerRuntimeScheduler;
import com.medievalfactions.registry.MFSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Authoritative world simulation hooks. All persistent changes occur on the logical server. */
@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MFWorldEvents {
    /** Weak keys ensure an integrated server's runtime state is released after its world closes. */
    private static final Map<MinecraftServer, MFServerRuntimeScheduler> SERVER_SCHEDULERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static MFServerRuntimeScheduler scheduler(MinecraftServer server) {
        synchronized (SERVER_SCHEDULERS) {
            return SERVER_SCHEDULERS.computeIfAbsent(server, ignored -> new MFServerRuntimeScheduler());
        }
    }
    private MFWorldEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long currentDay = event.getServer().overworld().getDayTime() / 24_000L;
        MFWorldSavedData world = MFWorldSavedData.get(event.getServer());
        world.observeMinecraftDay(currentDay);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(event.getServer());
        advanced.advanceToDay(world, MFFactionSavedData.get(event.getServer()), world.simulatedDays());
        MFServerRuntimeScheduler scheduler = scheduler(event.getServer());
        scheduler.advanceTick();
        // Footsteps, sword draw and location-aware ambience are rate-limited per player.
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            MFGameplayAudioEvents.tickPlayerAudio(player);
        }
        int minuteOfDay = (int)Math.floorMod(event.getServer().overworld().getDayTime(), 24_000L) * 1_440 / 24_000;
        if (scheduler.observeMinute(minuteOfDay)) {
            advanced.updateScheduledNpcPlans(world, minuteOfDay);
        }
        // Construction is real block placement on the logical server, batched to avoid a single-tick spike.
        if (scheduler.dueEvery(5L)) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                advanced.tickConstruction(level);
                advanced.tickDungeonProjects(level);
            }
        }
        if (scheduler.dueEvery(20L)) {
            // Resource regeneration and skill cooldowns run on the authoritative server, not on client ticks.
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                CompoundTag data = player.getPersistentData();
                data.putInt("mf_mana", Math.min(100, (data.contains("mf_mana", Tag.TAG_INT) ? data.getInt("mf_mana") : 100) + 3));
                data.putInt("mf_stamina", Math.min(100, (data.contains("mf_stamina", Tag.TAG_INT) ? data.getInt("mf_stamina") : 100) + 5));
                for (String key : new java.util.ArrayList<>(data.getAllKeys())) {
                    if (!key.startsWith("mf_cd_")) continue;
                    int remaining = data.getInt(key);
                    if (remaining <= 20) data.remove(key); else data.putInt(key, remaining - 20);
                }
            }
        }
        if (scheduler.dueEvery(400L)) {
            java.util.Set<java.util.UUID> ridden = new java.util.HashSet<>();
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (player.getVehicle() instanceof MFAnimatedMob mount) {
                    advanced.record("companions", mount.getUUID(), Companion.class)
                            .filter(c -> c.ownerId().equals(player.getUUID()) && c.alive())
                            .ifPresent(c -> {
                                Companion synchronizedHealth = c.withEntityHealth((int) Math.ceil(mount.getHealth()));
                                advanced.updateCompanion(synchronizedHealth);
                                if (synchronizedHealth.canRide(player.getUUID())) ridden.add(mount.getUUID());
                                else player.stopRiding();
                            });
                }
            }
            advanced.advanceCompanionUpkeep(ridden);
        }
    }

    /** Clears the resident entity link on death; the persistent citizen remains a separate authoritative record. */
    @SubscribeEvent
    public static void onResidentDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MFResidentEntity resident) || resident.citizenId() == null
                || !(resident.level() instanceof ServerLevel level)) return;
        MFAdvancedSavedData.get(level.getServer()).removeSimulationRecord("resident_links", resident.citizenId());
    }

    /** A killed persistent mount must also become dead in the companion ledger. */
    @SubscribeEvent
    public static void onCompanionDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MFAnimatedMob mount) || mount.getMountOwner() == null
                || !(mount.level() instanceof ServerLevel level)) return;
        MFAdvancedSavedData data = MFAdvancedSavedData.get(level.getServer());
        data.record("companions", mount.getUUID(), Companion.class).ifPresent(companion -> {
            if (companion.alive()) data.updateCompanion(companion.die());
        });
    }

    /** Keeps the persisted encounter state aligned when the physical Wither boss is killed. */
    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof WitherBoss boss) || !(boss.level() instanceof ServerLevel level)
                || !boss.getPersistentData().hasUUID("mf_boss_encounter")) return;
        var data = MFAdvancedSavedData.get(level.getServer());
        java.util.UUID encounterId = boss.getPersistentData().getUUID("mf_boss_encounter");
        data.record("boss_encounters", encounterId,
                com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class).ifPresent(current ->
                com.medievalfactions.domain.MFAdventureSystems.damageBoss(current, current.maxHealth(), 0)
                        .ifPresent(updated -> data.storeSimulationRecord("boss_encounters", encounterId, updated,
                                com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class)));
        data.removeSimulationRecord("boss_entities", encounterId);
    }

    /** Applies initial class combat passives and elemental damage matchups to real damage events. */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getNewDamage();
        if (damage <= 0.0F) return;

        if (target instanceof ServerPlayer defender) {
            var profile = MFWorldSavedData.get(defender.serverLevel().getServer()).profile(defender.getUUID());
            if (profile.isPresent()) {
                var element = profile.get().element();
                if (element == com.medievalfactions.domain.MFGameCatalog.Element.FIRE && source.is(DamageTypeTags.IS_FIRE)) damage *= 0.70F;
                else if (element == com.medievalfactions.domain.MFGameCatalog.Element.WATER && source.is(DamageTypeTags.IS_FIRE)) damage *= 0.90F;
                else if (element == com.medievalfactions.domain.MFGameCatalog.Element.ICE && source.is(DamageTypeTags.IS_FIRE)) damage *= 1.10F;
                else if (element == com.medievalfactions.domain.MFGameCatalog.Element.LIGHTNING && source.is(DamageTypeTags.IS_LIGHTNING)) damage *= 0.70F;
                else if (element == com.medievalfactions.domain.MFGameCatalog.Element.METAL && source.is(DamageTypeTags.IS_LIGHTNING)) damage *= 1.10F;
            }
        }

        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof ServerPlayer attacker && target != attacker && source.is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            var profile = MFWorldSavedData.get(attacker.serverLevel().getServer()).profile(attacker.getUUID());
            if (profile.isPresent()) {
                var combatClass = profile.get().mainClass();
                if (combatClass == com.medievalfactions.domain.MFGameCatalog.CombatClass.WARRIOR) damage *= 1.08F;
                else if (combatClass == com.medievalfactions.domain.MFGameCatalog.CombatClass.BERSERKER
                        && attacker.getHealth() <= attacker.getMaxHealth() * 0.50F) damage *= 1.18F;
                else if (combatClass == com.medievalfactions.domain.MFGameCatalog.CombatClass.ASSASSIN) {
                    var fromTargetToAttacker = attacker.position().subtract(target.position());
                    if (fromTargetToAttacker.lengthSqr() > 0.01D
                            && target.getLookAngle().normalize().dot(fromTargetToAttacker.normalize()) < -0.35D) damage *= 1.25F;
                } else if (combatClass == com.medievalfactions.domain.MFGameCatalog.CombatClass.LANCER
                        && attacker.getMainHandItem().is(Items.TRIDENT)) damage *= 1.12F;
            }
        }
        if (target instanceof WitherBoss wither && wither.getPersistentData().hasUUID("mf_boss_encounter")) {
            var advanced = MFAdvancedSavedData.get(((ServerLevel) wither.level()).getServer());
            java.util.UUID encounterId = wither.getPersistentData().getUUID("mf_boss_encounter");
            var encounter = advanced.record("boss_encounters", encounterId,
                    com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class).orElse(null);
            if (encounter != null && encounter.phase() != com.medievalfactions.domain.MFAdventureSystems.EncounterPhase.DEFEATED) {
                int incoming = Math.max(1, (int)Math.ceil(damage));
                com.medievalfactions.domain.MFAdventureSystems.damageBoss(encounter, incoming, 0)
                        .ifPresent(updated -> advanced.storeSimulationRecord("boss_encounters", encounterId,
                                updated, com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class));
            }
        }

        if (target instanceof ServerPlayer defender && damage > 0.0F) {
            MFGameplayAudioEvents.playArmorHit(defender);
        }
        if (Float.isFinite(damage)) event.setNewDamage(Math.max(0.0F, Math.min(2048.0F, damage)));
    }

    /** Converts eligible vanilla mob XP drops into separate class progression, not vanilla player XP. */
    @SubscribeEvent
    public static void onCombatExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) return;
        int vanillaXp = event.getOriginalExperience();
        if (vanillaXp <= 0) return;
        var server = player.serverLevel().getServer();
        long worldDay = server.overworld().getDayTime() / 24_000L;
        MFWorldSavedData world = MFWorldSavedData.get(server);
        int previousLevel = world.profile(player.getUUID()).map(profile -> profile.classLevel()).orElse(0);
        int awarded = world.awardCombatExperience(
                player.getUUID(), event.getEntity().getType().getDescriptionId(), vanillaXp, worldDay);
        if (awarded > 0) {
            int nextLevel = world.profile(player.getUUID()).map(profile -> profile.classLevel()).orElse(previousLevel);
            if (nextLevel > previousLevel) {
                player.playNotifySound(MFSounds.UI_LEVEL_UP.get(), SoundSource.PLAYERS, 0.85F, 1.0F);
            }
            // Intentional feedback without changing vanilla XP orb drops.
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Experiencia de combate MEDIEVAL FACTIONS: +" + awarded), true);
        }
    }
}
