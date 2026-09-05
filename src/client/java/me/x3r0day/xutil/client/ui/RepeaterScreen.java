package me.x3r0day.xutil.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import me.x3r0day.xutil.client.module.impl.misc.Repeater;
import me.x3r0day.xutil.client.repeater.Recording;
import me.x3r0day.xutil.client.repeater.RecordingStore;
import me.x3r0day.xutil.client.repeater.ReplayEngine;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RepeaterScreen extends Screen {

    private static final int ROW_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_WIDTH = 400;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_SELECTED = 0xFF3E3E58;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final Screen parent;
    private boolean editingRecord;
    private boolean editingPlay;

    public RepeaterScreen(Screen parent) {
        super(Component.literal("Recorder"));
        this.parent = parent;
    }

    private int controlRows() {
        return 3;
    }

    private int panelHeight() {
        int rows = Math.max(1, RecordingStore.getRecordings().size());
        return 38 + controlRows() * ROW_HEIGHT + rows * ROW_HEIGHT + 10 + BUTTON_HEIGHT + 14;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int listTop() {
        return panelY() + 38 + controlRows() * ROW_HEIGHT;
    }

    private int buttonsY() {
        return listTop() + Math.max(1, RecordingStore.getRecordings().size()) * ROW_HEIGHT + 10;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<Recording> recordings = RecordingStore.getRecordings();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, "Recorder", width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, editingRecord || editingPlay ? "Press a key, Esc to cancel"
            : "Bind keys, then record and replay in game", width / 2, panelY + 19,
            editingRecord || editingPlay ? GuiTheme.accent : COLOR_MUTED);

        int y = panelY + 38;
        keyRow(graphics, panelX, y, "Record key", Repeater.recordKey, editingRecord, mouseX, mouseY);
        y += ROW_HEIGHT;
        keyRow(graphics, panelX, y, "Play key", Repeater.playKey, editingPlay, mouseX, mouseY);
        y += ROW_HEIGHT;
        row(graphics, panelX, y, "Loop", Repeater.isLoop() ? "ON" : "OFF", mouseX, mouseY);

        if (recordings.isEmpty()) {
            graphics.centeredText(font, "No recordings yet", width / 2, listTop() + 8, COLOR_MUTED);
        }
        for (int i = 0; i < recordings.size(); i++) {
            Recording recording = recordings.get(i);
            int ry = listTop() + i * ROW_HEIGHT;
            boolean hovered = isOver(mouseX, mouseY, panelX + 16, ry, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
            boolean isSelected = recording.getName().equals(Repeater.getSelected());
            graphics.fill(panelX + 16, ry, panelX + PANEL_WIDTH - 16, ry + ROW_HEIGHT - 1,
                isSelected ? COLOR_SELECTED : hovered ? COLOR_HOVER : COLOR_ROW);

            String name = trimTo(recording.getName(), 22);
            graphics.text(font, name, panelX + 22, ry + 3,
                isSelected || hovered ? COLOR_TEXT : COLOR_MUTED, true);

            String time = String.format("%.1fs", recording.duration());
            graphics.text(font, time, panelX + PANEL_WIDTH - 80, ry + 3, COLOR_MUTED, true);

            int lockX = panelX + PANEL_WIDTH - 52;
            boolean hoverLock = isOver(mouseX, mouseY, lockX, ry, 30, ROW_HEIGHT - 1);
            graphics.text(font, recording.isLockMouse() ? "LOCK" : "free", lockX + 4, ry + 3,
                recording.isLockMouse() ? GuiTheme.accent : hoverLock ? COLOR_TEXT : COLOR_MUTED, true);
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 48) / 3;
        String[] labels = {"Delete", "Play", "Done"};
        int bx = panelX + 16;
        for (String label : labels) {
            boolean hovered = isOver(mouseX, mouseY, bx, buttonY, buttonWidth, BUTTON_HEIGHT);
            graphics.fill(bx, buttonY, bx + buttonWidth, buttonY + BUTTON_HEIGHT,
                hovered ? COLOR_HOVER : COLOR_ROW);
            graphics.centeredText(font, label, bx + buttonWidth / 2,
                buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
            bx += buttonWidth + 8;
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void keyRow(GuiGraphicsExtractor graphics, int panelX, int y, String label,
            InputConstants.Key key, boolean listening, int mouseX, int mouseY) {
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, label, panelX + 22, y + 3, COLOR_TEXT, true);
        String value = listening ? "..."
            : key.getValue() == InputConstants.UNKNOWN.getValue()
                ? "Not bound"
                : key.getDisplayName().getString();
        graphics.text(font, value, panelX + PANEL_WIDTH - 22 - font.width(value), y + 3,
            listening ? GuiTheme.accent : hovered ? COLOR_TEXT : COLOR_MUTED, true);
    }

    private void row(GuiGraphicsExtractor graphics, int panelX, int y, String label, String value,
            int mouseX, int mouseY) {
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, label, panelX + 22, y + 3, COLOR_TEXT, true);
        graphics.text(font, value, panelX + PANEL_WIDTH - 22 - font.width(value), y + 3,
            hovered ? COLOR_TEXT : COLOR_MUTED, true);
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
        if (editingRecord || editingPlay) {
            if (event.isEscape()) {
                editingRecord = false;
                editingPlay = false;
                return true;
            }
            InputConstants.Key key = InputConstants.getKey(event);
            if (editingRecord) {
                Repeater.recordKey = key;
                editingRecord = false;
            } else {
                Repeater.playKey = key;
                editingPlay = false;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);

        List<Recording> recordings = RecordingStore.getRecordings();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();

        int y = panelY + 38;
        if (isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
            editingRecord = true;
            editingPlay = false;
            return true;
        }
        y += ROW_HEIGHT;
        if (isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
            editingPlay = true;
            editingRecord = false;
            return true;
        }
        y += ROW_HEIGHT;
        if (isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
            Repeater.setLoop(!Repeater.isLoop());
            return true;
        }

        for (int i = 0; i < recordings.size(); i++) {
            Recording recording = recordings.get(i);
            int ry = listTop() + i * ROW_HEIGHT;
            int lockX = panelX + PANEL_WIDTH - 52;
            if (isOver(event.x(), event.y(), lockX, ry, 30, ROW_HEIGHT - 1)) {
                recording.setLockMouse(!recording.isLockMouse());
                RecordingStore.save();
                return true;
            }
            if (isOver(event.x(), event.y(), panelX + 16, ry, PANEL_WIDTH - 32, ROW_HEIGHT - 1)) {
                Repeater.setSelected(recording.getName());
                return true;
            }
        }

        int buttonY = buttonsY();
        int buttonWidth = (PANEL_WIDTH - 48) / 3;
        int bx = panelX + 16;
        if (isOver(event.x(), event.y(), bx, buttonY, buttonWidth, BUTTON_HEIGHT)) {
            RecordingStore.remove(Repeater.getSelected());
            Repeater.setSelected("");
            return true;
        }
        bx += buttonWidth + 8;
        if (isOver(event.x(), event.y(), bx, buttonY, buttonWidth, BUTTON_HEIGHT)) {
            Recording recording = RecordingStore.getByName(Repeater.getSelected());
            if (recording != null) {
                ReplayEngine.play(recording);
                onClose();
            }
            return true;
        }
        bx += buttonWidth + 8;
        if (isOver(event.x(), event.y(), bx, buttonY, buttonWidth, BUTTON_HEIGHT)) {
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
