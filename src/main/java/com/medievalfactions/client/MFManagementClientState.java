package com.medievalfactions.client;

import com.medievalfactions.network.MFManagementRequestPayload;
import com.medievalfactions.network.MFManagementSnapshotPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-thread cache for the latest server-authoritative management snapshot. */
public final class MFManagementClientState {
    private static int page;
    private static String title = "MEDIEVAL FACTIONS";
    private static String body = "Conecta a un mundo para cargar los datos de gestión.";
    private static boolean pending;

    private MFManagementClientState() {}

    public static void request(int requestedPage) {
        int safePage = Math.max(0, Math.min(MFManagementRequestPayload.PAGE_COUNT - 1, requestedPage));
        page = safePage;
        pending = true;
        body = "Consultando el estado del mundo en el servidor...";
        if (Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().player == null) {
            pending = false;
            body = "No hay conexión con un servidor. Entra en un mundo para consultar la gestión.";
            return;
        }
        PacketDistributor.sendToServer(new MFManagementRequestPayload(safePage));
    }

    public static void accept(MFManagementSnapshotPayload snapshot) {
        if (snapshot == null || snapshot.page() != page) return;
        title = snapshot.title();
        body = snapshot.body();
        pending = false;
    }

    public static int page() { return page; }
    public static String title() { return title; }
    public static String body() { return body; }
    public static boolean pending() { return pending; }
}
