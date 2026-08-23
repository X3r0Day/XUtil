package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.Macro;
import me.x3r0day.xutil.client.macro.MacroManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MacroListScreen extends Screen {

    private static final int ROW_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 400;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_ACCENT = 0xFF8A5CFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final Screen parent;

    public MacroListScreen(Screen parent) {
        super(Component.literal("Macros"));
        this.parent = parent;
    }

    private int panelHeight() {
        int rows = Math.max(1, MacroManager.getMacros().size());
        return 38 + rows * ROW_HEIGHT + 38;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<Macro> macros = MacroManager.getMacros();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listTop = panelY + 38;
        int listBottom = listTop + Math.max(1, macros.size()) * ROW_HEIGHT;
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, COLOR_ACCENT);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "Macros", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, "Click a macro to edit its task chain", width / 2, panelY + 19, COLOR_MUTED);

        if (macros.isEmpty()) {
            graphics.centeredText(font, "No macros yet", width / 2, listTop + 8, COLOR_MUTED);
        }
        for (int i = 0; i < macros.size(); i++) {
            Macro macro = macros.get(i);
            int y = listTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= panelX + 4 && mouseX < panelX + PANEL_WIDTH - 4
                && mouseY >= y && mouseY < y + ROW_HEIGHT;
            graphics.fill(panelX + 4, y, panelX + PANEL_WIDTH - 4, y + ROW_HEIGHT - 1,
                hovered ? COLOR_HOVER : COLOR_ROW);
            graphics.text(font, macro.getName(), panelX + 8, y + 3, COLOR_TEXT, true);

            String info = macro.getTrigger().label() + " | " + (macro.isKeyUnbound()
                ? "unbound" : macro.getKey().getDisplayName().getString())
                + " | " + macro.getTasks().size() + " tasks";
            graphics.text(font, trimTo(info, 30), panelX + PANEL_WIDTH - 12 - font.width(trimTo(info, 30)),
                y + 3, hovered ? COLOR_TEXT : COLOR_MUTED, true);
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
        graphics.centeredText(font, "New Macro", buttonLeft + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
        graphics.centeredText(font, "Done", buttonRight + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
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
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);

        List<Macro> macros = MacroManager.getMacros();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listTop = panelY + 38;

        if (event.x() >= panelX + 4 && event.x() < panelX + PANEL_WIDTH - 4
            && event.y() >= listTop) {
            int row = (int) ((event.y() - listTop) / ROW_HEIGHT);
            if (row >= 0 && row < macros.size()) {
                minecraft.gui.setScreen(new MacroEditorScreen(macros.get(row), this));
                return true;
            }
        }

        int buttonY = listTop + Math.max(1, macros.size()) * ROW_HEIGHT + 8;
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        if (isOver(event.x(), event.y(), panelX + 16, buttonY, buttonWidth)) {
            newMacro();
            return true;
        }
        if (isOver(event.x(), event.y(), panelX + PANEL_WIDTH - 16 - buttonWidth, buttonY, buttonWidth)) {
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private void newMacro() {
        String name;
        int n = 1;
        boolean taken;
        do {
            name = "Macro " + n++;
            taken = false;
            for (Macro existing : MacroManager.getMacros()) {
                if (existing.getName().equals(name)) {
                    taken = true;
                    break;
                }
            }
        } while (taken);
        Macro macro = MacroManager.addMacro(name);
        minecraft.gui.setScreen(new MacroEditorScreen(macro, this));
    }

    private static boolean isOver(double mx, double my, int x, int y, int w) {
        return mx >= x && mx < x + w && my >= y && my < y + BUTTON_HEIGHT;
    }

    private static String trimTo(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
