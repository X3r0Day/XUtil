package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.addon.AddonLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AddonWarningScreen extends Screen {

    private static final int ROW_HEIGHT = 14;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 340;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_ACCENT = 0xFF8A5CFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;
    private static final int COLOR_ERROR = 0xFFFF6B6B;

    private final Screen parent;

    public AddonWarningScreen(Screen parent) {
        super(Component.literal("XUtil Addon Warnings"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<AddonLoader.Failure> failures = AddonLoader.getFailures();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - panelHeight(failures)) / 2;
        int listTop = panelY + 38;
        int listBottom = listTop + failures.size() * ROW_HEIGHT;
        int panelBottom = panelY + panelHeight(failures);

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, COLOR_ACCENT);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "Addons failed to load", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, failures.size() + " addon(s) hit an error during startup",
            width / 2, panelY + 19, COLOR_MUTED);

        for (int row = 0; row < failures.size(); row++) {
            AddonLoader.Failure failure = failures.get(row);
            int y = listTop + row * ROW_HEIGHT;
            graphics.fill(panelX + 4, y, panelX + PANEL_WIDTH - 4, y + ROW_HEIGHT - 1, COLOR_ROW);
            graphics.text(font, trim(failure.modId() + " (" + failure.name() + "): " + failure.error()),
                panelX + 8, y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_ERROR, true);
        }

        int buttonY = listBottom + 8;
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        int buttonLeft = panelX + 16;
        int buttonRight = panelX + PANEL_WIDTH - 16 - buttonWidth;

        boolean hoverLeft = isOver(mouseX, mouseY, buttonLeft, buttonY, buttonWidth);
        boolean hoverRight = isOver(mouseX, mouseY, buttonRight, buttonY, buttonWidth);

        graphics.fill(buttonLeft, buttonY, buttonLeft + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverLeft ? COLOR_HOVER : COLOR_ROW);
        graphics.fill(buttonRight, buttonY, buttonRight + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverRight ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Open Logs Folder", buttonLeft + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
        graphics.centeredText(font, "Continue", buttonRight + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);

        graphics.centeredText(font, "Check the log for details, or continue to the title screen",
            width / 2, panelBottom + 7, COLOR_MUTED);
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
            List<AddonLoader.Failure> failures = AddonLoader.getFailures();
            int panelX = (width - PANEL_WIDTH) / 2;
            int panelY = (height - panelHeight(failures)) / 2;
            int listBottom = panelY + 38 + failures.size() * ROW_HEIGHT;
            int buttonY = listBottom + 8;
            int buttonWidth = (PANEL_WIDTH - 40) / 2;

            if (isOver(event.x(), event.y(), panelX + 16, buttonY, buttonWidth)) {
                openLogsFolder();
                return true;
            }
            if (isOver(event.x(), event.y(), panelX + PANEL_WIDTH - 16 - buttonWidth, buttonY, buttonWidth)) {
                onClose();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private void openLogsFolder() {
        Util.getPlatform().openPath(FabricLoader.getInstance().getGameDir().resolve("logs"));
    }

    private int panelHeight(List<AddonLoader.Failure> failures) {
        return 38 + failures.size() * ROW_HEIGHT + 36;
    }

    private boolean isOver(double mouseX, double mouseY, int x, int y, int w) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
    }

    private static String trim(String text) {
        int max = 60;
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
