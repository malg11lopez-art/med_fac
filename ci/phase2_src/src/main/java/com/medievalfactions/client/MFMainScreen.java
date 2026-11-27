package com.medievalfactions.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * In-game management GUI backed by a server snapshot. The screen is read-only:
 * all authoritative changes still go through validated server commands.
 */
public final class MFMainScreen extends Screen {
    private static final String[] PAGES = {
            "Reino", "Habitantes", "Ciudades", "Ejército",
            "Comercio", "Dragones", "Familia / salud", "Aventuras"
    };
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 390;
    private int panelLeft;
    private int panelTop;

    public MFMainScreen() {
        super(Component.literal("MEDIEVAL FACTIONS — Gestión del mundo"));
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        int panelHeight = Math.min(PANEL_HEIGHT, height - 16);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        int tabGap = 5;
        int tabWidth = Math.max(74, (panelWidth - 24 - tabGap * 3) / 4);
        int tabHeight = 21;
        int tabsTop = panelTop + 48;
        for (int i = 0; i < PAGES.length; i++) {
            final int selectedPage = i;
            int column = i % 4;
            int row = i / 4;
            int x = panelLeft + 12 + column * (tabWidth + tabGap);
            int y = tabsTop + row * (tabHeight + 5);
            addRenderableWidget(Button.builder(Component.literal(PAGES[i]),
                    button -> requestPage(selectedPage))
                    .bounds(x, y, tabWidth, tabHeight).build());
        }

        int footerY = panelTop + panelHeight - 29;
        addRenderableWidget(Button.builder(Component.literal("Actualizar"),
                button -> requestPage(MFManagementClientState.page()))
                .bounds(panelLeft + 12, footerY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Panel de comandos"),
                button -> sendCommand("mf advanced dashboard"))
                .bounds(panelLeft + 120, footerY, 132, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cambiar skin"),
                button -> {
                    MFPlayerSkinClient.cycleSkin();
                    requestPage(MFManagementClientState.page());
                }).bounds(panelLeft + 260, footerY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cerrar"),
                button -> Minecraft.getInstance().setScreen(null))
                .bounds(panelLeft + panelWidth - 112, footerY, 100, 20).build());

        requestPage(MFManagementClientState.page());
    }

    private void requestPage(int page) {
        MFManagementClientState.request(page);
    }

    private void sendCommand(String command) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null && minecraft.player != null) {
            minecraft.getConnection().sendCommand(command);
        }
        minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        int panelHeight = Math.min(PANEL_HEIGHT, height - 16);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF00D1420);
        graphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFFD4B77A);
        graphics.fill(panelLeft + 1, panelTop + 1, panelLeft + panelWidth - 1, panelTop + 36, 0xF0212D3C);
        graphics.drawCenteredString(font, Component.literal("MEDIEVAL FACTIONS — GESTIÓN DEL MUNDO"),
                width / 2, panelTop + 9, 0xFFFFE6B2);
        graphics.drawCenteredString(font, Component.literal(MFManagementClientState.title()
                        + (MFManagementClientState.pending() ? " • actualizando…" : "")),
                width / 2, panelTop + 27, 0xFFB8C9D8);

        int textX = panelLeft + 15;
        int textY = panelTop + 105;
        int lineHeight = 12;
        int maxLines = Math.max(1, (panelHeight - 143) / lineHeight);
        String[] lines = MFManagementClientState.body().split("\\R", -1);
        int shown = 0;
        for (String line : lines) {
            if (shown >= maxLines) break;
            String remaining = line;
            int maxChars = Math.max(24, (panelWidth - 32) / 6);
            while (!remaining.isEmpty() && shown < maxLines) {
                int split = Math.min(maxChars, remaining.length());
                if (split < remaining.length()) {
                    int space = remaining.lastIndexOf(' ', split);
                    if (space > maxChars / 2) split = space;
                }
                graphics.drawString(font, remaining.substring(0, split), textX, textY + shown * lineHeight,
                        shown == 0 ? 0xFFFFD98B : 0xFFE2E8F0, false);
                remaining = remaining.substring(split).stripLeading();
                shown++;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
