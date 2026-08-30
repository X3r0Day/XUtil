package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.MacroManager;
import me.x3r0day.xutil.client.macro.MacroStatement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StatementEditScreen extends Screen {

    private static final int ROW_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 400;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final MacroStatement statement;
    private final Screen parent;

    public StatementEditScreen(MacroStatement statement, Screen parent) {
        super(Component.literal("Conditions"));
        this.statement = statement;
        this.parent = parent;
    }

    private int panelHeight() {
        return 38 + ROW_HEIGHT + Math.max(1, statement.getParts().size()) * ROW_HEIGHT
            + 10 + BUTTON_HEIGHT + 14;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int operatorY() {
        return panelY() + 38;
    }

    private int listTop() {
        return operatorY() + ROW_HEIGHT;
    }

    private int buttonsY() {
        return listTop() + Math.max(1, statement.getParts().size()) * ROW_HEIGHT + 10;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<MacroStatement.Part> parts = statement.getParts();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "Conditions", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, "Click a row to edit, click NOT to negate", width / 2,
            panelY + 19, COLOR_MUTED);

        int opY = operatorY();
        boolean hoverOp = isOver(mouseX, mouseY, panelX + 16, opY, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, opY, panelX + PANEL_WIDTH - 16, opY + ROW_HEIGHT - 1,
            hoverOp ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, "Operator", panelX + 22, opY + 3, COLOR_TEXT, true);
        String opLabel = statement.getOperator().label();
        graphics.text(font, opLabel, panelX + PANEL_WIDTH - 22 - font.width(opLabel), opY + 3,
            hoverOp ? COLOR_TEXT : COLOR_MUTED, true);

        if (parts.isEmpty()) {
            graphics.centeredText(font, "No conditions, click Add", width / 2, listTop() + 8, COLOR_MUTED);
        }
        for (int i = 0; i < parts.size(); i++) {
            MacroStatement.Part part = parts.get(i);
            int y = listTop() + i * ROW_HEIGHT;
            boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
            graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
                hovered ? COLOR_HOVER : COLOR_ROW);

            boolean hoverRemove = isOver(mouseX, mouseY, panelX + 20, y, 16, ROW_HEIGHT - 1);
            graphics.text(font, "x", panelX + 24, y + 3, hoverRemove ? 0xFFFF5C5C : COLOR_MUTED, true);

            int notX = panelX + PANEL_WIDTH - 52;
            boolean hoverNot = isOver(mouseX, mouseY, notX, y, 30, ROW_HEIGHT - 1);
            graphics.text(font, "NOT", notX + 4, y + 3,
                part.negate() ? GuiTheme.accent : hoverNot ? COLOR_TEXT : COLOR_MUTED, true);

            String desc = trimTo(part.condition().description(), 38);
            graphics.text(font, desc, panelX + 40, y + 3, hovered ? COLOR_TEXT : COLOR_MUTED, true);
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        int leftX = panelX + 16;
        int rightX = panelX + PANEL_WIDTH - 16 - buttonWidth;
        boolean hoverAdd = isOver(mouseX, mouseY, leftX, buttonY, buttonWidth, BUTTON_HEIGHT);
        boolean hoverDone = isOver(mouseX, mouseY, rightX, buttonY, buttonWidth, BUTTON_HEIGHT);
        graphics.fill(leftX, buttonY, leftX + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverAdd ? COLOR_HOVER : COLOR_ROW);
        graphics.fill(rightX, buttonY, rightX + buttonWidth, buttonY + BUTTON_HEIGHT,
            hoverDone ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Add condition", leftX + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
        graphics.centeredText(font, "Done", rightX + buttonWidth / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
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
        List<MacroStatement.Part> parts = statement.getParts();

        if (isOver(event.x(), event.y(), panelX + 16, operatorY(), PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
            statement.setOperator(statement.getOperator().next());
            MacroManager.save();
            return true;
        }

        for (int i = 0; i < parts.size(); i++) {
            int y = listTop() + i * ROW_HEIGHT;
            if (!isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
                continue;
            }
            if (isOver(event.x(), event.y(), panelX + 20, y, 16, ROW_HEIGHT - 1)) {
                statement.remove(i);
                MacroManager.save();
                return true;
            }
            int notX = panelX + PANEL_WIDTH - 52;
            if (isOver(event.x(), event.y(), notX, y, 30, ROW_HEIGHT - 1)) {
                MacroStatement.Part part = parts.get(i);
                statement.getParts().set(i, new MacroStatement.Part(part.condition(), !part.negate()));
                MacroManager.save();
                return true;
            }
            minecraft.gui.setScreen(new ConditionPickerScreen(statement, i, this));
            return true;
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 40) / 2;
        if (isOver(event.x(), event.y(), panelX + 16, buttonY, buttonWidth, BUTTON_HEIGHT)) {
            minecraft.gui.setScreen(new ConditionPickerScreen(statement, -1, this));
            return true;
        }
        if (isOver(event.x(), event.y(), panelX + PANEL_WIDTH - 16 - buttonWidth, buttonY,
            buttonWidth, BUTTON_HEIGHT)) {
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private static boolean isOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static String trimTo(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
