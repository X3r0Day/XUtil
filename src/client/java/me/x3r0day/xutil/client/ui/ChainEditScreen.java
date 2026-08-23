package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.MacroManager;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ChainEditScreen extends Screen {

    protected static final int ROW_HEIGHT = 16;
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int PANEL_WIDTH = 420;
    protected static final int COLOR_PANEL = 0xEE121218;
    protected static final int COLOR_ROW = 0xC01B1B24;
    protected static final int COLOR_HOVER = 0xFF333345;
    protected static final int COLOR_SELECTED = 0xFF3E3E58;
    protected static final int COLOR_ACCENT = 0xFF8A5CFF;
    protected static final int COLOR_TEXT = 0xFFFFFFFF;
    protected static final int COLOR_MUTED = 0xFFAAAAAF;

    protected final List<MacroTask> chain;
    protected final Screen parent;
    private final String screenTitle;
    private int selected = -1;

    public ChainEditScreen(String title, List<MacroTask> chain, Screen parent) {
        super(Component.literal(title));
        this.screenTitle = title;
        this.chain = chain;
        this.parent = parent;
    }

    protected int extraHeaderRows() {
        return 0;
    }

    protected int extraButtonRows() {
        return 0;
    }

    protected void renderExtraHeader(GuiGraphicsExtractor graphics, int panelX, int panelY,
            int mouseX, int mouseY) {
    }

    protected boolean clickExtraHeader(MouseButtonEvent event, int panelX, int panelY) {
        return false;
    }

    protected void renderExtraButtons(GuiGraphicsExtractor graphics, int panelX, int buttonY,
            int mouseX, int mouseY) {
    }

    protected boolean clickExtraButtons(MouseButtonEvent event, int panelX, int buttonY) {
        return false;
    }

    protected void onChanged() {
        MacroManager.save();
    }

    public void addTaskToList(MacroTask task) {
        chain.add(task);
        onChanged();
    }

    private int panelHeight() {
        int rows = Math.max(1, chain.size());
        return 38 + extraHeaderRows() * ROW_HEIGHT + rows * ROW_HEIGHT + 18
            + (1 + extraButtonRows()) * (BUTTON_HEIGHT + 6);
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int listTop() {
        return panelY() + 38 + extraHeaderRows() * ROW_HEIGHT;
    }

    private int listBottom() {
        return listTop() + Math.max(1, chain.size()) * ROW_HEIGHT;
    }

    private int buttonsTop() {
        return listBottom() + 10;
    }

    protected static boolean isOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected static String trimTo(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listTop = listTop();
        int listBottom = listBottom();
        int panelBottom = panelY + panelHeight();
        int buttonsTop = buttonsTop();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, COLOR_ACCENT);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, screenTitle, width / 2, panelY + 7, COLOR_TEXT);

        renderExtraHeader(graphics, panelX, panelY, mouseX, mouseY);

        if (chain.isEmpty()) {
            graphics.centeredText(font, "Empty - click Add", width / 2, listTop + 8, COLOR_MUTED);
        }
        for (int i = 0; i < chain.size(); i++) {
            int y = listTop + i * ROW_HEIGHT;
            boolean hovered = isOver(mouseX, mouseY, panelX + 4, y, PANEL_WIDTH - 8, ROW_HEIGHT);
            graphics.fill(panelX + 4, y, panelX + PANEL_WIDTH - 4, y + ROW_HEIGHT - 1,
                i == selected ? COLOR_SELECTED : hovered ? COLOR_HOVER : COLOR_ROW);
            graphics.text(font, trimTo((i + 1) + ". " + chain.get(i).description(), 48),
                panelX + 8, y + 3, i == selected || hovered ? COLOR_TEXT : COLOR_MUTED, true);
        }

        String[] labels = {"Add", "Edit", "Up", "Down", "Delete", "Done"};
        int buttonWidth = 56;
        int gap = 6;
        int total = labels.length * buttonWidth + (labels.length - 1) * gap;
        int bx = panelX + (PANEL_WIDTH - total) / 2;
        for (String label : labels) {
            boolean hovered = isOver(mouseX, mouseY, bx, buttonsTop, buttonWidth, BUTTON_HEIGHT);
            graphics.fill(bx, buttonsTop, bx + buttonWidth, buttonsTop + BUTTON_HEIGHT,
                hovered ? COLOR_HOVER : COLOR_ROW);
            graphics.centeredText(font, label, bx + buttonWidth / 2,
                buttonsTop + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
            bx += buttonWidth + gap;
        }

        renderExtraButtons(graphics, panelX, buttonsTop + BUTTON_HEIGHT + 6, mouseX, mouseY);
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

        int panelX = (width - PANEL_WIDTH) / 2;
        int listTop = listTop();
        int listBottom = listBottom();
        int buttonsTop = buttonsTop();

        if (clickExtraHeader(event, panelX, panelY())) return true;

        if (isOver(event.x(), event.y(), panelX + 4, listTop, PANEL_WIDTH - 8, listBottom - listTop)) {
            int row = (int) ((event.y() - listTop) / ROW_HEIGHT);
            if (row >= 0 && row < chain.size()) {
                selected = row;
                return true;
            }
        }

        String[] labels = {"Add", "Edit", "Up", "Down", "Delete", "Done"};
        int buttonWidth = 56;
        int gap = 6;
        int total = labels.length * buttonWidth + (labels.length - 1) * gap;
        int bx = panelX + (PANEL_WIDTH - total) / 2;
        for (int i = 0; i < labels.length; i++) {
            if (isOver(event.x(), event.y(), bx, buttonsTop, buttonWidth, BUTTON_HEIGHT)) {
                switch (i) {
                    case 0 -> minecraft.gui.setScreen(new AddTaskScreen(this));
                    case 1 -> editSelected();
                    case 2 -> moveSelected(-1);
                    case 3 -> moveSelected(1);
                    case 4 -> deleteSelected();
                    case 5 -> onClose();
                }
                return true;
            }
            bx += buttonWidth + gap;
        }

        if (clickExtraButtons(event, panelX, buttonsTop + BUTTON_HEIGHT + 6)) return true;

        return super.mouseClicked(event, doubleClick);
    }

    private void editSelected() {
        if (selected < 0 || selected >= chain.size()) return;
        minecraft.gui.setScreen(new TaskEditScreen(chain.get(selected), this));
    }

    private void moveSelected(int dir) {
        if (selected < 0) return;
        int target = selected + dir;
        if (target < 0 || target >= chain.size()) return;
        Collections.swap(chain, selected, target);
        selected = target;
        onChanged();
    }

    private void deleteSelected() {
        if (selected < 0 || selected >= chain.size()) return;
        chain.remove(selected);
        if (selected >= chain.size()) {
            selected = chain.size() - 1;
        }
        onChanged();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
