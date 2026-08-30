package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.MacroTasks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AddTaskScreen extends Screen {

    private static final int ROW_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 380;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final ChainEditScreen parent;

    public AddTaskScreen(ChainEditScreen parent) {
        super(Component.literal("Add Task"));
        this.parent = parent;
    }

    private int panelHeight() {
        return 38 + MacroTasks.TYPES.size() * ROW_HEIGHT + 38;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<MacroTasks.TaskType> types = MacroTasks.TYPES;
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listTop = panelY + 38;
        int listBottom = listTop + types.size() * ROW_HEIGHT;
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "Add Task", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, "Click a task type to add it", width / 2, panelY + 19, COLOR_MUTED);

        for (int i = 0; i < types.size(); i++) {
            MacroTasks.TaskType type = types.get(i);
            int y = listTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= panelX + 4 && mouseX < panelX + PANEL_WIDTH - 4
                && mouseY >= y && mouseY < y + ROW_HEIGHT;
            graphics.fill(panelX + 4, y, panelX + PANEL_WIDTH - 4, y + ROW_HEIGHT - 1,
                hovered ? COLOR_HOVER : COLOR_ROW);
            String text = type.name() + " - " + type.description();
            graphics.text(font, trimTo(text, 44), panelX + 8, y + 3,
                hovered ? COLOR_TEXT : COLOR_MUTED, true);
        }

        int buttonY = listBottom + 8;
        boolean hovered = mouseX >= panelX + 16 && mouseX < panelX + PANEL_WIDTH - 16
            && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;
        graphics.fill(panelX + 16, buttonY, panelX + PANEL_WIDTH - 16, buttonY + BUTTON_HEIGHT,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Cancel", panelX + PANEL_WIDTH / 2,
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

        List<MacroTasks.TaskType> types = MacroTasks.TYPES;
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listTop = panelY + 38;

        if (event.x() >= panelX + 4 && event.x() < panelX + PANEL_WIDTH - 4
            && event.y() >= listTop) {
            int row = (int) ((event.y() - listTop) / ROW_HEIGHT);
            if (row >= 0 && row < types.size()) {
                MacroTask task = types.get(row).factory().get();
                parent.addTaskToList(task);
                minecraft.gui.setScreen(new TaskEditScreen(task, parent));
                return true;
            }
        }

        int buttonY = listTop + types.size() * ROW_HEIGHT + 8;
        if (event.x() >= panelX + 16 && event.x() < panelX + PANEL_WIDTH - 16
            && event.y() >= buttonY && event.y() < buttonY + BUTTON_HEIGHT) {
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private static String trimTo(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
