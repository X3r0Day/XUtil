package me.x3r0day.xutil.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import me.x3r0day.xutil.client.macro.Macro;
import me.x3r0day.xutil.client.macro.MacroManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class MacroEditorScreen extends ChainEditScreen {

    private final Macro macro;
    private boolean listening;

    public MacroEditorScreen(Macro macro, Screen parent) {
        super(macro.getName(), macro.getTasks(), parent);
        this.macro = macro;
    }

    @Override
    protected int extraHeaderRows() {
        return 2;
    }

    @Override
    protected int extraButtonRows() {
        return 1;
    }

    @Override
    protected void renderExtraHeader(GuiGraphicsExtractor graphics, int panelX, int panelY,
            int mouseX, int mouseY) {
        int triggerY = panelY + 38;
        boolean hoverTrigger = isOver(mouseX, mouseY, panelX + 16, triggerY, PANEL_WIDTH - 32, ROW_HEIGHT);
        graphics.fill(panelX + 16, triggerY, panelX + PANEL_WIDTH - 16, triggerY + ROW_HEIGHT - 1,
            hoverTrigger ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, "Trigger", panelX + 22, triggerY + 3, COLOR_TEXT, true);
        String triggerLabel = macro.getTrigger().label();
        graphics.text(font, triggerLabel, panelX + PANEL_WIDTH - 22 - font.width(triggerLabel), triggerY + 3,
            hoverTrigger ? COLOR_TEXT : COLOR_MUTED, true);

        int keyY = triggerY + ROW_HEIGHT;
        boolean hoverKey = isOver(mouseX, mouseY, panelX + 16, keyY, PANEL_WIDTH - 32, ROW_HEIGHT);
        graphics.fill(panelX + 16, keyY, panelX + PANEL_WIDTH - 16, keyY + ROW_HEIGHT - 1,
            hoverKey ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, "Keybind", panelX + 22, keyY + 3, COLOR_TEXT, true);
        String key = listening ? "..." : macro.isKeyUnbound()
            ? "unbound" : macro.getKey().getDisplayName().getString();
        graphics.text(font, key, panelX + PANEL_WIDTH - 22 - font.width(key), keyY + 3,
            listening ? COLOR_ACCENT : COLOR_MUTED, true);
    }

    @Override
    protected boolean clickExtraHeader(MouseButtonEvent event, int panelX, int panelY) {
        int triggerY = panelY + 38;
        if (isOver(event.x(), event.y(), panelX + 16, triggerY, PANEL_WIDTH - 32, ROW_HEIGHT)) {
            macro.setTrigger(macro.getTrigger().next());
            onChanged();
            return true;
        }
        int keyY = triggerY + ROW_HEIGHT;
        if (isOver(event.x(), event.y(), panelX + 16, keyY, PANEL_WIDTH - 32, ROW_HEIGHT)) {
            listening = true;
            return true;
        }
        return false;
    }

    @Override
    protected void renderExtraButtons(GuiGraphicsExtractor graphics, int panelX, int buttonY,
            int mouseX, int mouseY) {
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, buttonY, PANEL_WIDTH - 32, BUTTON_HEIGHT);
        graphics.fill(panelX + 16, buttonY, panelX + PANEL_WIDTH - 16, buttonY + BUTTON_HEIGHT,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Delete Macro", panelX + PANEL_WIDTH / 2,
            buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2, hovered ? COLOR_TEXT : COLOR_MUTED);
    }

    @Override
    protected boolean clickExtraButtons(MouseButtonEvent event, int panelX, int buttonY) {
        if (isOver(event.x(), event.y(), panelX + 16, buttonY, PANEL_WIDTH - 32, BUTTON_HEIGHT)) {
            MacroManager.removeMacro(macro);
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening) {
            if (event.isEscape()) {
                listening = false;
                return true;
            }
            macro.setKey(InputConstants.getKey(event));
            MacroManager.save();
            listening = false;
            return true;
        }
        return super.keyPressed(event);
    }
}
