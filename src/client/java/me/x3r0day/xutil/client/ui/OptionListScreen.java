package me.x3r0day.xutil.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OptionListScreen extends Screen {

    public record KeybindRow(String label, KeyMapping keybind) {
    }

    private static final int ROW_HEIGHT = 14;
    private static final int HEADER_AREA = 38;
    private static final int PANEL_MIN_WIDTH = 220;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;
    private static final int COLOR_ON = 0xFF62E67D;
    private static final int COLOR_OFF = 0xFF9A9AA5;

    private final String title;
    private final String subtitle;
    private final List<OptionToggle> toggles;
    private final List<KeybindRow> keybinds;
    private final Screen parent;
    private KeyMapping listening;

    public OptionListScreen(String title, String subtitle, List<OptionToggle> toggles, Screen parent) {
        this(title, subtitle, toggles, List.of(), parent);
    }

    public OptionListScreen(String title, String subtitle, List<OptionToggle> toggles,
            List<KeybindRow> keybinds, Screen parent) {
        super(Component.literal(title));
        this.title = title;
        this.subtitle = subtitle;
        this.toggles = toggles;
        this.keybinds = keybinds;
        this.parent = parent;
    }

    private int totalRows() {
        return toggles.size() + keybinds.size();
    }

    private int panelHeight() {
        return HEADER_AREA + totalRows() * ROW_HEIGHT + 18;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelWidth = panelWidth();
        int panelX = (width - panelWidth) / 2;
        int panelY = panelY();
        int listTop = panelY + HEADER_AREA;
        int listBottom = listTop + totalRows() * ROW_HEIGHT;
        int panelBottom = listBottom + 18;

        graphics.fill(0, 0, width, height, 0x90000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, title, width / 2, panelY + 7, COLOR_TEXT);
        String subtitleText = listening != null
            ? "Press a key, Esc to cancel"
            : subtitle;
        graphics.centeredText(font, subtitleText, width / 2, panelY + 19,
            listening != null ? GuiTheme.accent : COLOR_MUTED);

        for (int row = 0; row < totalRows(); row++) {
            int y = listTop + row * ROW_HEIGHT;
            boolean hovered = mouseX >= panelX + 4 && mouseX < panelX + panelWidth - 4
                && mouseY >= y && mouseY < y + ROW_HEIGHT;
            graphics.fill(panelX + 4, y, panelX + panelWidth - 4, y + ROW_HEIGHT - 1,
                hovered ? COLOR_HOVER : COLOR_ROW);

            if (row < toggles.size()) {
                OptionToggle toggle = toggles.get(row);
                graphics.text(font, toggle.label(), panelX + 8,
                    y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT, true);
                boolean on = toggle.isOn().getAsBoolean();
                String state = on ? "ON" : "OFF";
                graphics.text(font, state,
                    panelX + panelWidth - 8 - font.width(state),
                    y + (ROW_HEIGHT - font.lineHeight) / 2,
                    on ? COLOR_ON : COLOR_OFF, true);
            } else {
                KeybindRow keybindRow = keybinds.get(row - toggles.size());
                graphics.text(font, keybindRow.label(), panelX + 8,
                    y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT, true);
                String key = listening == keybindRow.keybind()
                    ? "..."
                    : keybindRow.keybind().getTranslatedKeyMessage().getString();
                graphics.text(font, key,
                    panelX + panelWidth - 8 - font.width(key),
                    y + (ROW_HEIGHT - font.lineHeight) / 2,
                    listening == keybindRow.keybind() ? GuiTheme.accent : COLOR_MUTED, true);
            }
        }

        graphics.centeredText(font, "Esc to close", width / 2, panelBottom + 7, COLOR_MUTED);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != null) {
            if (event.isEscape()) {
                listening = null;
                return true;
            }
            listening.setKey(InputConstants.getKey(event));
            listening = null;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int panelWidth = panelWidth();
            int panelX = (width - panelWidth) / 2;
            int listTop = panelY() + HEADER_AREA;
            if (event.x() >= panelX + 4 && event.x() < panelX + panelWidth - 4
                && event.y() >= listTop) {
                int row = (int) ((event.y() - listTop) / ROW_HEIGHT);
                if (row >= 0 && row < totalRows()) {
                    if (row < toggles.size()) {
                        toggles.get(row).toggle().run();
                    } else {
                        listening = keybinds.get(row - toggles.size()).keybind();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int panelWidth() {
        int widthForText = Math.max(PANEL_MIN_WIDTH, font.width(subtitle) + 24);
        return Math.min(widthForText, width - 20);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
