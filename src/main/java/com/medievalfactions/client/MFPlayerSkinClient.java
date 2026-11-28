package com.medievalfactions.client;

import com.medievalfactions.MedievalFactions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import com.medievalfactions.network.MFPlayerSkinSelectionPayload;
import com.medievalfactions.network.MFPlayerSkinRules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Local player cosmetic selector. Selection is stored per Minecraft client. */
public final class MFPlayerSkinClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MFPlayerSkinClient.class);
    private static final List<ResourceLocation> SKINS = List.of(
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_arcane_swordsman.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_archer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_assassin.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_bard.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_berserker.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_blacksmith.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_cleric.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_crossbowman.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_druid.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_lancer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_mage.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_monk.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_necromancer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_noble.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_paladin.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_ranger.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_summoner.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/player_warrior.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_alchemist.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_banker.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_blacksmith.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_builder.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_carpenter.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_courier.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_engineer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_farmer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_fisher.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_guard.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_healer.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_hunter.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_lumberjack.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_merchant.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_miner.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_priest.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_scholar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_shepherd.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_tailor.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/profession_tanner.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_beastkin.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_beastkin_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_beastkin_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_celestial.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_celestial_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_celestial_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_centaur.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_centaur_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_centaur_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_demon.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_demon_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_demon_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_draconid.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_draconid_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_draconid_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_dwarf.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_dwarf_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_dwarf_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_elf.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_elf_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_elf_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_fae.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_fae_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_fae_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_giant.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_giant_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_giant_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_goblin.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_goblin_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_goblin_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_harpy.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_harpy_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_harpy_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_human.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_human_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_human_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_merfolk.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_merfolk_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_merfolk_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_orc.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_orc_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_orc_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_undead.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_undead_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_undead_solar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_vampire.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_vampire_lunar.png"),
            ResourceLocation.fromNamespaceAndPath(MedievalFactions.MOD_ID, "textures/player/race_vampire_solar.png")
    );
    private static final String CONFIG_FILE = "medieval_factions_skin.txt";
    private static int selectedIndex = -1;
    private static boolean loaded;
    private static ClientPacketListener lastSyncedConnection;
    private static final Map<UUID, Integer> REMOTE_SKINS = new HashMap<>();

    private MFPlayerSkinClient() {}

    public static ResourceLocation currentSkin() {
        ensureLoaded();
        return SKINS.get(selectedIndex);
    }

    public static int selectedIndex() {
        ensureLoaded();
        return selectedIndex;
    }

    public static int skinCount() {
        return SKINS.size();
    }

    /** Returns this player's cosmetic, or null when a remote player has no modded skin. */
    public static ResourceLocation skinFor(AbstractClientPlayer player) {
        if (player == null) return null;
        ensureLoaded();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && player.getUUID().equals(minecraft.player.getUUID())) {
            return SKINS.get(selectedIndex);
        }
        Integer remoteIndex = REMOTE_SKINS.get(player.getUUID());
        return remoteIndex != null && MFPlayerSkinRules.isValidIndex(remoteIndex) ? SKINS.get(remoteIndex) : null;
    }

    /** Called by the client payload handler after the server broadcasts a validated selection. */
    public static void setRemoteSkin(UUID playerId, int skinIndex) {
        if (playerId == null) return;
        if (MFPlayerSkinRules.isValidIndex(skinIndex)) REMOTE_SKINS.put(playerId, skinIndex);
        else REMOTE_SKINS.remove(playerId);
        LOGGER.info("MF_SKIN_SYNC_RECEIVED player={} index={}", playerId, skinIndex);
    }

    /** Synchronizes the local preference once for each new multiplayer/LAN/singleplayer connection. */
    public static void synchronizeWithServerIfNeeded() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null || minecraft.player == null) {
            if (connection == null) {
                lastSyncedConnection = null;
                REMOTE_SKINS.clear();
            }
            return;
        }
        if (connection != lastSyncedConnection) {
            lastSyncedConnection = connection;
            REMOTE_SKINS.clear();
            sendSelectionToServer();
        }
    }

    private static void sendSelectionToServer() {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new MFPlayerSkinSelectionPayload(selectedIndex()));
        }
    }

    public static void cycleSkin() {
        ensureLoaded();
        selectedIndex = (selectedIndex + 1) % SKINS.size();
        saveSelection();
        sendSelectionToServer();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.medieval_factions.skin_selected",
                            selectedIndex + 1, SKINS.size(), SKINS.get(selectedIndex).getPath()),
                    true);
        }
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        selectedIndex = 0;
        try {
            Path config = configPath();
            if (Files.isRegularFile(config)) {
                String raw = Files.readString(config, StandardCharsets.UTF_8).trim();
                int parsed = Integer.parseInt(raw);
                if (parsed >= 0 && parsed < SKINS.size()) selectedIndex = parsed;
            }
        } catch (IOException | NumberFormatException ignored) {
            // A missing or damaged cosmetic preference must never prevent client startup.
        }
    }

    private static void saveSelection() {
        try {
            Path config = configPath();
            Files.createDirectories(config.getParent());
            Files.writeString(config, Integer.toString(selectedIndex), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Rendering still works for this session if the preference cannot be persisted.
        }
    }

    private static Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(CONFIG_FILE);
    }
}
