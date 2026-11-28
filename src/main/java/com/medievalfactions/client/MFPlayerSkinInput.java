package com.medievalfactions.client;

import com.medievalfactions.MedievalFactions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client game-bus input handler for the player skin selector. */
@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class MFPlayerSkinInput {
    private static final Logger LOGGER = LoggerFactory.getLogger(MFPlayerSkinInput.class);
    private static boolean ciAutoJoinAttempted;
    private static int ciAutoJoinWaitTicks;

    public static final KeyMapping CYCLE_SKIN = new KeyMapping(
            "key.medieval_factions.cycle_player_skin",
            78,
            "key.categories.medieval_factions");

    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.medieval_factions.open_menu",
            79,
            "key.categories.medieval_factions");

    private MFPlayerSkinInput() {}

    /**
     * Test-only connection probe. CI enables this through a JVM system property;
     * ordinary client sessions never connect to an arbitrary address automatically.
     */
    private static void tryCiNetworkAutoJoin() {
        if (ciAutoJoinAttempted) return;
        String address = System.getProperty("mf.ci.autoJoin");
        if (address == null || address.isBlank()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null || minecraft.player != null) {
            ciAutoJoinAttempted = true;
            return;
        }
        if (minecraft.screen == null || ++ciAutoJoinWaitTicks < 20) return;
        ciAutoJoinAttempted = true;
        try {
            ServerAddress serverAddress = ServerAddress.parseString(address.strip());
            ServerData serverData = new ServerData("MEDIEVAL FACTIONS CI", address.strip(), ServerData.Type.OTHER);
            LOGGER.info("MF_CI_AUTOJOIN_CONNECT address={}", address.strip());
            ConnectScreen.startConnecting(minecraft.screen, minecraft, serverAddress, serverData, false, null);
        } catch (RuntimeException exception) {
            LOGGER.error("MF_CI_AUTOJOIN_FAILED address={}", address, exception);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tryCiNetworkAutoJoin();
        MFPlayerSkinClient.synchronizeWithServerIfNeeded();
        while (OPEN_MENU.consumeClick()) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().setScreen(new MFMainScreen());
            }
        }
        while (CYCLE_SKIN.consumeClick()) {
            MFPlayerSkinClient.cycleSkin();
        }
    }
}
