package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.MacroCondition;
import me.x3r0day.xutil.client.macro.MacroConditions;
import me.x3r0day.xutil.client.macro.MacroManager;
import me.x3r0day.xutil.client.macro.MacroStatement;
import me.x3r0day.xutil.client.macro.condition.EntityInRangeCondition;
import me.x3r0day.xutil.client.macro.condition.HealthAboveCondition;
import me.x3r0day.xutil.client.macro.condition.HealthBelowCondition;
import me.x3r0day.xutil.client.macro.condition.HungerBelowCondition;
import me.x3r0day.xutil.client.macro.condition.TimeOfDayCondition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ConditionPickerScreen extends Screen {

    private static final int ROW_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 380;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final MacroStatement statement;
    private final int partIndex;
    private final Screen parent;

    private MacroConditions.Group group;
    private MacroCondition preview;

    public ConditionPickerScreen(MacroStatement statement, int partIndex, Screen parent) {
        super(Component.literal(partIndex >= 0 ? "Replace condition" : "Add condition"));
        this.statement = statement;
        this.partIndex = partIndex;
        this.parent = parent;
    }

    private int rows() {
        if (group == null) {
            return MacroConditions.Group.values().length;
        }
        int n = MacroConditions.byGroup(group).size();
        return 1 + n + (hasParam(preview) ? 1 : 0);
    }

    private static boolean hasParam(MacroCondition condition) {
        return condition instanceof EntityInRangeCondition || condition instanceof HealthBelowCondition
            || condition instanceof HealthAboveCondition || condition instanceof HungerBelowCondition
            || condition instanceof TimeOfDayCondition;
    }

    private int panelHeight() {
        return 38 + rows() * ROW_HEIGHT + 10 + BUTTON_HEIGHT + 14;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int listTop() {
        return panelY() + 38;
    }

    private int buttonsY() {
        return listTop() + rows() * ROW_HEIGHT + 10;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, getTitle().getString(), width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, group == null ? "Pick a group" : "Pick a condition", width / 2,
            panelY + 19, COLOR_MUTED);

        int row = 0;
        if (group == null) {
            MacroConditions.Group[] groups = MacroConditions.Group.values();
            for (MacroConditions.Group value : groups) {
                row(graphics, panelX, row++, value.label(), false, mouseX, mouseY);
            }
        } else {
            row(graphics, panelX, row++, "< Back", false, mouseX, mouseY);
            List<MacroConditions.ConditionType> types = MacroConditions.byGroup(group);
            for (MacroConditions.ConditionType type : types) {
                boolean selected = preview != null && preview.type().equals(type.factory().get().type());
                row(graphics, panelX, row++, type.name(), selected, mouseX, mouseY);
            }
            if (hasParam(preview)) {
                paramRow(graphics, panelX, row, mouseX, mouseY);
            }
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        int leftX = panelX + 16;
        int rightX = panelX + PANEL_WIDTH - 16 - buttonWidth;
        boolean hoverPick = group != null && isOver(mouseX, mouseY, leftX, buttonY, buttonWidth, BUTTON_HEIGHT);
        boolean hoverCancel = isOver(mouseX, mouseY, rightX, buttonY, buttonWidth, BUTTON_HEIGHT);
        graphics.fill(leftX, buttonY, leftX + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverPick ? COLOR_HOVER : COLOR_ROW);
        graphics.fill(rightX, buttonY, rightX + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverCancel ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Pick", leftX + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, group == null ? COLOR_MUTED : COLOR_TEXT);
        graphics.centeredText(font, "Cancel", rightX + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void row(GuiGraphicsExtractor graphics, int panelX, int row, String label,
            boolean selected, int mouseX, int mouseY) {
        int y = listTop() + row * ROW_HEIGHT;
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
            hovered ? COLOR_HOVER : COLOR_ROW);
        int color = selected ? GuiTheme.accent : hovered ? COLOR_TEXT : COLOR_MUTED;
        graphics.text(font, label, panelX + 22, y + 3, color, true);
    }

    private void paramRow(GuiGraphicsExtractor graphics, int panelX, int row, int mouseX, int mouseY) {
        int y = listTop() + row * ROW_HEIGHT;
        String label;
        String value;
        boolean cycle = false;
        if (preview instanceof EntityInRangeCondition range) {
            label = "Radius";
            value = String.valueOf((int) range.getRadius());
        } else if (preview instanceof HealthBelowCondition health) {
            label = "Health";
            value = String.valueOf((int) health.getThreshold());
        } else if (preview instanceof HealthAboveCondition health) {
            label = "Health";
            value = String.valueOf((int) health.getThreshold());
        } else if (preview instanceof HungerBelowCondition hunger) {
            label = "Hunger";
            value = String.valueOf(hunger.getThreshold());
        } else {
            TimeOfDayCondition time = (TimeOfDayCondition) preview;
            label = "Time";
            value = time.getTime().label();
            cycle = true;
        }
        graphics.text(font, label, panelX + 22, y + 3, COLOR_TEXT, true);
        if (cycle) {
            boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
            graphics.text(font, value, panelX + PANEL_WIDTH - 22 - font.width(value), y + 3,
                hovered ? COLOR_TEXT : COLOR_MUTED, true);
            return;
        }
        int minusX = panelX + PANEL_WIDTH - 96;
        int plusX = panelX + PANEL_WIDTH - 36;
        boolean hoverMinus = isOver(mouseX, mouseY, minusX, y, 28, ROW_HEIGHT - 1);
        boolean hoverPlus = isOver(mouseX, mouseY, plusX, y, 28, ROW_HEIGHT - 1);
        graphics.fill(minusX, y, minusX + 28, y + ROW_HEIGHT - 1, hoverMinus ? COLOR_HOVER : COLOR_ROW);
        graphics.fill(plusX, y, plusX + 28, y + ROW_HEIGHT - 1, hoverPlus ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "-", minusX + 14, y + 3, COLOR_TEXT);
        graphics.centeredText(font, "+", plusX + 14, y + 3, COLOR_TEXT);
        graphics.text(font, value, minusX - 10 - font.width(value), y + 3, COLOR_MUTED, true);
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

        if (group == null) {
            MacroConditions.Group[] groups = MacroConditions.Group.values();
            for (int i = 0; i < groups.length; i++) {
                if (clickRow(event, panelX, i)) {
                    group = groups[i];
                    preview = MacroConditions.byGroup(group).get(0).factory().get();
                    return true;
                }
            }
        } else {
            List<MacroConditions.ConditionType> types = MacroConditions.byGroup(group);
            int row = 0;
            if (clickRow(event, panelX, row)) {
                group = null;
                preview = null;
                return true;
            }
            row++;
            for (MacroConditions.ConditionType type : types) {
                if (clickRow(event, panelX, row)) {
                    preview = type.factory().get();
                    return true;
                }
                row++;
            }
            if (hasParam(preview) && clickParam(event, panelX, row)) {
                return true;
            }
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        if (group != null && isOver(event.x(), event.y(), panelX + 16, buttonY, buttonWidth, BUTTON_HEIGHT)) {
            if (partIndex >= 0) {
                statement.replace(partIndex, preview);
            } else {
                statement.add(preview, false);
            }
            MacroManager.save();
            onClose();
            return true;
        }
        if (isOver(event.x(), event.y(), panelX + PANEL_WIDTH - 16 - buttonWidth, buttonY,
            buttonWidth, BUTTON_HEIGHT)) {
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickRow(MouseButtonEvent event, int panelX, int row) {
        int y = listTop() + row * ROW_HEIGHT;
        return isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
    }

    private boolean clickParam(MouseButtonEvent event, int panelX, int row) {
        int y = listTop() + row * ROW_HEIGHT;
        if (preview instanceof TimeOfDayCondition time) {
            if (isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
                time.setTime(time.getTime().next());
                return true;
            }
            return false;
        }
        int minusX = panelX + PANEL_WIDTH - 96;
        int plusX = panelX + PANEL_WIDTH - 36;
        int delta = 0;
        if (isOver(event.x(), event.y(), minusX, y, 28, ROW_HEIGHT - 1)) delta = -1;
        if (isOver(event.x(), event.y(), plusX, y, 28, ROW_HEIGHT - 1)) delta = 1;
        if (delta == 0) return false;
        if (preview instanceof EntityInRangeCondition range) {
            range.setRadius(Mth.clamp((int) range.getRadius() + delta, 1, 64));
        } else if (preview instanceof HealthBelowCondition health) {
            health.setThreshold(Mth.clamp((int) health.getThreshold() + delta, 1, 20));
        } else if (preview instanceof HealthAboveCondition health) {
            health.setThreshold(Mth.clamp((int) health.getThreshold() + delta, 1, 20));
        } else if (preview instanceof HungerBelowCondition hunger) {
            hunger.setThreshold(Mth.clamp(hunger.getThreshold() + delta, 1, 19));
        }
        return true;
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private static boolean isOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
