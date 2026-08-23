package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.impl.world.WorldInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class WorldInfoScreen extends Screen {

    private static final int ROW_HEIGHT = 14;
    private static final int PANEL_Y = 20;
    private static final int HEADER_AREA = 38;
    private static final int PANEL_MIN_WIDTH = 220;
    private static final String SUBTITLE = "Click a row to toggle what the overlay shows";
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_ACCENT = 0xFF8A5CFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;
    private static final int COLOR_ON = 0xFF62E67D;
    private static final int COLOR_OFF = 0xFF9A9AA5;

    private final Screen parent;

    public WorldInfoScreen(Screen parent) {
        super(Component.literal("World Info"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelWidth = panelWidth();
        int panelX = (width - panelWidth) / 2;
        int panelY = PANEL_Y;
        int listTop = panelY + HEADER_AREA;
        int listBottom = listTop + WorldInfo.TOGGLES.size() * ROW_HEIGHT;
        int panelBottom = listBottom + 18;

        graphics.fill(0, 0, width, height, 0x90000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelBottom + 1, COLOR_ACCENT);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "World Info", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, SUBTITLE, width / 2, panelY + 19, COLOR_MUTED);

        for (int row = 0; row < WorldInfo.TOGGLES.size(); row++) {
            WorldInfo.InfoToggle toggle = WorldInfo.TOGGLES.get(row);
            int y = listTop + row * ROW_HEIGHT;
            boolean hovered = mouseX >= panelX + 4 && mouseX < panelX + panelWidth - 4
                && mouseY >= y && mouseY < y + ROW_HEIGHT;
            graphics.fill(panelX + 4, y, panelX + panelWidth - 4, y + ROW_HEIGHT - 1,
                hovered ? COLOR_HOVER : COLOR_ROW);

            graphics.text(font, toggle.label(), panelX + 8, y + (ROW_HEIGHT - font.lineHeight) / 2,
                COLOR_TEXT, true);
            boolean on = toggle.isOn().getAsBoolean();
            String state = on ? "ON" : "OFF";
            graphics.text(font, state,
                panelX + panelWidth - 8 - font.width(state), y + (ROW_HEIGHT - font.lineHeight) / 2,
                on ? COLOR_ON : COLOR_OFF, true);
        }

        String footer = "Esc to close";
        graphics.centeredText(font, footer, width / 2, panelBottom + 7, COLOR_MUTED);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int panelWidth = panelWidth();
            int panelX = (width - panelWidth) / 2;
            int listTop = PANEL_Y + HEADER_AREA;
            if (event.x() >= panelX + 4 && event.x() < panelX + panelWidth - 4
                && event.y() >= listTop) {
                int row = (int) ((event.y() - listTop) / ROW_HEIGHT);
                if (row >= 0 && row < WorldInfo.TOGGLES.size()) {
                    WorldInfo.TOGGLES.get(row).toggle().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int panelWidth() {
        int widthForText = Math.max(PANEL_MIN_WIDTH, font.width(SUBTITLE) + 24);
        return Math.min(widthForText, width - 20);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
