package com.medievalfactions.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
    private EditBox actionInput;

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

        int actionY = panelTop + panelHeight - 57;
        actionInput = new EditBox(font, panelLeft + 12, actionY, Math.max(180, panelWidth - 116), 20,
                Component.literal("Acción del servidor"));
        addRenderableWidget(Button.builder(Component.literal("Preparar consulta"),
                button -> fillSuggestedCommand())
                .bounds(panelLeft + 12, actionY - 24, 130, 18).build());
        actionInput.setMaxLength(160);
        actionInput.setHint(Component.literal("mf advanced ..."));
        addRenderableWidget(actionInput);
        addRenderableWidget(Button.builder(Component.literal("Ejecutar"), button -> executeManagementCommand())
                .bounds(panelLeft + panelWidth - 94, actionY, 82, 20).build());

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
        addRenderableWidget(Button.builder(Component.literal("Planificar IA"),
                button -> replanResidents())
                .bounds(panelLeft + 366, footerY, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cerrar"),
                button -> Minecraft.getInstance().setScreen(null))
                .bounds(panelLeft + panelWidth - 112, footerY, 100, 20).build());

        requestPage(MFManagementClientState.page());
    }

    private void requestPage(int page) {
        MFManagementClientState.request(page);
    }

    /** Fills the command editor with a valid read/list action for the selected dashboard section. */
    private void fillSuggestedCommand() {
        if (actionInput == null) return;
        String command = switch (MFManagementClientState.page()) {
            case 1 -> "mf advanced npc list";
            case 3 -> "mf advanced warfare army list";
            case 4 -> "mf advanced caravan list";
            case 5 -> "mf advanced companion list";
            case 6 -> "mf advanced family status";
            case 7 -> "mf advanced expedition list";
            default -> "mf advanced dashboard";
        };
        actionInput.setValue(command);
        actionInput.setFocused(true);
    }

    private void executeManagementCommand() {
        Minecraft minecraft = Minecraft.getInstance();
        if (actionInput == null || minecraft.player == null || minecraft.getConnection() == null) return;
        String command = actionInput.getValue().trim();
        if (command.startsWith("/")) command = command.substring(1).trim();
        if (command.isEmpty() || command.length() > 160 || command.contains(";")
                || command.contains("\n") || command.contains("\r")) {
            minecraft.player.displayClientMessage(Component.literal("Acción rechazada: introduce un solo comando breve."), true);
            return;
        }
        String[] parts = command.split("\\s+", 3);
        if (parts.length < 2 || !parts[0].equals("mf")
                || !java.util.Set.of("advanced", "character", "faction", "calendar").contains(parts[1])) {
            minecraft.player.displayClientMessage(Component.literal("Solo se permiten operaciones de MEDIEVAL FACTIONS (/mf)."), true);
            return;
        }
        minecraft.getConnection().sendCommand(command);
        actionInput.setValue("");
        requestPage(MFManagementClientState.page());
    }

    /** Requests the server-authoritative daily resident planner. The server enforces operator permission. */
    private void replanResidents() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) return;
        minecraft.getConnection().sendCommand("mf advanced npc plan");
        minecraft.player.displayClientMessage(
                Component.literal("Solicitud enviada: recalculando horarios y trabajo de habitantes."), true);
        requestPage(MFManagementClientState.page());
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
        // Reserve space for the suggestion button, command editor and footer controls.
        int maxLines = Math.max(1, (panelHeight - 190) / lineHeight);
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
