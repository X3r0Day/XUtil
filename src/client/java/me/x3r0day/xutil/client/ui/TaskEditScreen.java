package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.macro.MacroManager;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.task.AttackTask;
import me.x3r0day.xutil.client.macro.task.BreakTask;
import me.x3r0day.xutil.client.macro.task.ChatTask;
import me.x3r0day.xutil.client.macro.task.IfTask;
import me.x3r0day.xutil.client.macro.task.HotbarTask;
import me.x3r0day.xutil.client.macro.task.JumpTask;
import me.x3r0day.xutil.client.macro.task.LookTask;
import me.x3r0day.xutil.client.macro.task.LoopTask;
import me.x3r0day.xutil.client.macro.task.MoveTask;
import me.x3r0day.xutil.client.macro.task.ReplayTask;
import me.x3r0day.xutil.client.macro.task.ToggleModuleTask;
import me.x3r0day.xutil.client.macro.task.UseTask;
import me.x3r0day.xutil.client.macro.task.WaitTask;
import me.x3r0day.xutil.client.macro.task.WaitUntilTask;
import me.x3r0day.xutil.client.repeater.RecordingStore;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class TaskEditScreen extends Screen {

    private static final int PANEL_WIDTH = 340;
    private static final int ROW_HEIGHT = 22;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_ROW = 0xC01B1B24;
    private static final int COLOR_HOVER = 0xFF333345;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;

    private final MacroTask task;
    private final Screen parent;
    private EditBox messageField;

    public TaskEditScreen(MacroTask task, Screen parent) {
        super(Component.literal(taskTypeName(task)));
        this.task = task;
        this.parent = parent;
    }

    private static String taskTypeName(MacroTask task) {
        return switch (task.type()) {
            case "chat" -> "Chat Task";
            case "attack" -> "Attack Task";
            case "wait" -> "Wait Task";
            case "move" -> "Walk Task";
            case "jump" -> "Jump Task";
            case "look" -> "Turn Task";
            case "module" -> "Module Task";
            case "if" -> "If Task";
            case "loop" -> "Loop Task";
            case "break" -> "Break Task";
            case "use" -> "Use Task";
            case "wait_until" -> "Wait Until Task";
            case "hotbar" -> "Hotbar Task";
            case "replay" -> "Replay Task";
            default -> "Task";
        };
    }

    private int rows() {
        if (task instanceof ChatTask) return 1;
        if (task instanceof WaitTask) return 1;
        if (task instanceof MoveTask) return 1;
        if (task instanceof LookTask look) return look.getMode() == LookTask.Mode.TURN ? 3 : 5;
        if (task instanceof ToggleModuleTask) return 2;
        if (task instanceof LoopTask) return 3;
        if (task instanceof WaitUntilTask) return 1;
        if (task instanceof IfTask) return 3;
        if (task instanceof HotbarTask) return 1;
        if (task instanceof AttackTask attack) return 2 + (attack.getMode() == AttackTask.Mode.CROSSHAIR ? 0 : 2);
        if (task instanceof ReplayTask) return 1;
        return 0;
    }

    private int panelHeight() {
        if (task instanceof JumpTask || task instanceof BreakTask
            || task instanceof UseTask) return 120;
        return 50 + rows() * ROW_HEIGHT + 46;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int rowTop(int index) {
        return panelY() + 50 + index * ROW_HEIGHT;
    }

    private int doneTop() {
        return panelY() + panelHeight() - 30;
    }

    @Override
    protected void init() {
        if (task instanceof ChatTask chat) {
            int x = (width - PANEL_WIDTH) / 2;
            messageField = new EditBox(font, x + 16, rowTop(0) + 12, PANEL_WIDTH - 32, 20,
                Component.literal("Message"));
            messageField.setMaxLength(256);
            messageField.setValue(chat.getMessage());
            addRenderableWidget(messageField);
            setInitialFocus(messageField);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int panelBottom = panelY + panelHeight();

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelBottom + 1, GuiTheme.accent);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, COLOR_PANEL);
        graphics.centeredText(font, taskTypeName(task), width / 2, panelY + 7, COLOR_TEXT);
        graphics.centeredText(font, task.description(), width / 2, panelY + 19, COLOR_MUTED);

        if (task instanceof ChatTask) {
            graphics.text(font, "Message", panelX + 16, rowTop(0), COLOR_TEXT, false);
        } else if (task instanceof AttackTask attack) {
            cycleRow(graphics, panelX, 0, "Mode", attack.getMode().label(), mouseX, mouseY);
            cycleRow(graphics, panelX, 1, "Wait cooldown", attack.isWaitCooldown() ? "ON" : "OFF",
                mouseX, mouseY);
            if (attack.getMode() != AttackTask.Mode.CROSSHAIR) {
                stepperRow(graphics, panelX, 2, "Range", (int) attack.getRange(), mouseX, mouseY);
                cycleRow(graphics, panelX, 3, "Filter", attack.getFilter().label(), mouseX, mouseY);
            }
        } else if (task instanceof WaitTask wait) {
            stepperRow(graphics, panelX, 0, "Ticks", wait.getTicks(), mouseX, mouseY);
        } else if (task instanceof MoveTask move) {
            stepperRow(graphics, panelX, 0, "Ticks", move.getTicks(), mouseX, mouseY);
        } else if (task instanceof LookTask look) {
            cycleRow(graphics, panelX, 0, "Mode", look.getMode().label(), mouseX, mouseY);
            if (look.getMode() == LookTask.Mode.TURN) {
                stepperRow(graphics, panelX, 1, "Yaw", (int) look.getYaw(), mouseX, mouseY);
                stepperRow(graphics, panelX, 2, "Pitch", (int) look.getPitch(), mouseX, mouseY);
            } else {
                stepperRow(graphics, panelX, 1, "Range", (int) look.getRange(), mouseX, mouseY);
                cycleRow(graphics, panelX, 2, "Sort", look.getSort().label(), mouseX, mouseY);
                cycleRow(graphics, panelX, 3, "Filter", look.getFilter().label(), mouseX, mouseY);
                cycleRow(graphics, panelX, 4, "Smooth aim", look.isSmooth() ? "ON" : "OFF",
                    mouseX, mouseY);
            }
        } else if (task instanceof ToggleModuleTask toggleModule) {
            cycleRow(graphics, panelX, 0, "Module", moduleDisplay(toggleModule), mouseX, mouseY);
            cycleRow(graphics, panelX, 1, "Action", toggleModule.getAction().label(), mouseX, mouseY);
        } else if (task instanceof LoopTask loop) {
            cycleRow(graphics, panelX, 0, "Mode", loop.isInfinite() ? "Infinite" : "Repeat N times",
                mouseX, mouseY);
            stepperRow(graphics, panelX, 1, "Times", loop.getTimes(), mouseX, mouseY);
            buttonRow(graphics, panelX, 2, "Edit body (" + loop.getBody().size() + " tasks)", mouseX, mouseY);
        } else if (task instanceof HotbarTask hotbar) {
            stepperRow(graphics, panelX, 0, "Slot", hotbar.getSlot(), mouseX, mouseY);
        } else if (task instanceof ReplayTask replay) {
            String value = replay.getRecording().isEmpty() ? "<none>" : replay.getRecording();
            cycleRow(graphics, panelX, 0, "Recording", value, mouseX, mouseY);
        } else if (task instanceof WaitUntilTask waitUntil) {
            buttonRow(graphics, panelX, 0, "Conditions (" + waitUntil.getStatement().getParts().size() + ")",
                mouseX, mouseY);
        } else if (task instanceof IfTask ifTask) {
            buttonRow(graphics, panelX, 0, "Conditions (" + ifTask.getStatement().getParts().size() + ")",
                mouseX, mouseY);
            buttonRow(graphics, panelX, 1, "Edit then chain (" + ifTask.getThenTasks().size() + " tasks)",
                mouseX, mouseY);
            buttonRow(graphics, panelX, 2, "Edit else chain (" + ifTask.getElseTasks().size() + " tasks)",
                mouseX, mouseY);
        }

        int doneTop = doneTop();
        boolean hoverDone = isOver(mouseX, mouseY, panelX + PANEL_WIDTH / 2 - 40, doneTop, 80, BUTTON_HEIGHT);
        graphics.fill(panelX + PANEL_WIDTH / 2 - 40, doneTop, panelX + PANEL_WIDTH / 2 + 40,
            doneTop + BUTTON_HEIGHT, hoverDone ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "Done", panelX + PANEL_WIDTH / 2,
            doneTop + (BUTTON_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);

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
        int doneTop = doneTop();
        if (isOver(event.x(), event.y(), panelX + PANEL_WIDTH / 2 - 40, doneTop, 80, BUTTON_HEIGHT)) {
            save();
            onClose();
            return true;
        }

        if (task instanceof WaitTask wait) {
            applyStepper(event, panelX, 0, wait.getTicks(), 5, 1, 6000, wait::setTicks);
        } else if (task instanceof AttackTask attack) {
            if (clickCycle(event, panelX, 0)) {
                attack.setMode(attack.getMode().next());
                MacroManager.save();
                return true;
            }
            if (clickCycle(event, panelX, 1)) {
                attack.setWaitCooldown(!attack.isWaitCooldown());
                MacroManager.save();
                return true;
            }
            if (attack.getMode() != AttackTask.Mode.CROSSHAIR) {
                applyStepper(event, panelX, 2, (int) attack.getRange(), 1, 1, 64,
                    value -> attack.setRange(value));
                if (clickCycle(event, panelX, 3)) {
                    attack.setFilter(attack.getFilter().next());
                    MacroManager.save();
                    return true;
                }
            }
        } else if (task instanceof MoveTask move) {
            applyStepper(event, panelX, 0, move.getTicks(), 5, 1, 6000, move::setTicks);
        } else if (task instanceof LookTask look) {
            if (clickCycle(event, panelX, 0)) {
                look.setMode(look.getMode().next());
                MacroManager.save();
                return true;
            }
            if (look.getMode() == LookTask.Mode.TURN) {
                applyStepper(event, panelX, 1, (int) look.getYaw(), 5, -180, 180,
                    value -> look.setYaw(value));
                applyStepper(event, panelX, 2, (int) look.getPitch(), 5, -90, 90,
                    value -> look.setPitch(value));
            } else {
                applyStepper(event, panelX, 1, (int) look.getRange(), 1, 1, 64,
                    value -> look.setRange(value));
                if (clickCycle(event, panelX, 2)) {
                    look.setSort(look.getSort().next());
                    MacroManager.save();
                    return true;
                }
                if (clickCycle(event, panelX, 3)) {
                    look.setFilter(look.getFilter().next());
                    MacroManager.save();
                    return true;
                }
                if (clickCycle(event, panelX, 4)) {
                    look.setSmooth(!look.isSmooth());
                    MacroManager.save();
                    return true;
                }
            }
        } else if (task instanceof ToggleModuleTask toggleModule) {
            if (clickCycle(event, panelX, 0)) {
                cycleModule(toggleModule);
                MacroManager.save();
            } else if (clickCycle(event, panelX, 1)) {
                toggleModule.setAction(toggleModule.getAction().next());
                MacroManager.save();
            }
        } else if (task instanceof LoopTask loop) {
            if (clickCycle(event, panelX, 0)) {
                loop.setInfinite(!loop.isInfinite());
                MacroManager.save();
                return true;
            }
            applyStepper(event, panelX, 1, loop.getTimes(), 1, 1, 1000, loop::setTimes);
            if (clickButton(event, panelX, 2)) {
                minecraft.gui.setScreen(new ChainEditScreen("Loop Body", loop.getBody(), this));
            }
        } else if (task instanceof HotbarTask hotbar) {
            applyStepper(event, panelX, 0, hotbar.getSlot(), 1, 1, 9, hotbar::setSlot);
        } else if (task instanceof ReplayTask replay) {
            if (clickCycle(event, panelX, 0)) {
                cycleRecording(replay);
                MacroManager.save();
                return true;
            }
        } else if (task instanceof WaitUntilTask waitUntil) {
            if (clickButton(event, panelX, 0)) {
                minecraft.gui.setScreen(new StatementEditScreen(waitUntil.getStatement(), this));
            }
        } else if (task instanceof IfTask ifTask) {
            if (clickButton(event, panelX, 0)) {
                minecraft.gui.setScreen(new StatementEditScreen(ifTask.getStatement(), this));
                return true;
            }
            if (clickButton(event, panelX, 1)) {
                minecraft.gui.setScreen(new ChainEditScreen("If Then", ifTask.getThenTasks(), this));
                return true;
            }
            if (clickButton(event, panelX, 2)) {
                minecraft.gui.setScreen(new ChainEditScreen("If Else", ifTask.getElseTasks(), this));
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private static String moduleDisplay(ToggleModuleTask task) {
        if (!task.getModuleName().isEmpty()) return task.getModuleName();
        List<Module> modules = ModuleManager.getModules();
        return modules.isEmpty() ? "No modules" : "Choose module";
    }

    private void cycleRecording(ReplayTask task) {
        List<String> names = RecordingStore.names();
        if (names.isEmpty()) {
            task.setRecording("");
            return;
        }
        int index = names.indexOf(task.getRecording());
        task.setRecording(names.get((index + 1) % names.size()));
    }

    private void cycleModule(ToggleModuleTask task) {
        List<Module> modules = ModuleManager.getModules();
        if (modules.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).getName().equals(task.getModuleName())) {
                index = i;
                break;
            }
        }
        index = (index + 1) % modules.size();
        task.setModuleName(modules.get(index).getName());
    }


    private void stepperRow(GuiGraphicsExtractor graphics, int panelX, int row, String label,
            int value, int mouseX, int mouseY) {
        int y = rowTop(row);
        graphics.text(font, label, panelX + 22, y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT, true);

        int minusX = panelX + PANEL_WIDTH - 96;
        int plusX = panelX + PANEL_WIDTH - 36;
        boolean hoverMinus = isOver(mouseX, mouseY, minusX, y, 28, ROW_HEIGHT - 1);
        boolean hoverPlus = isOver(mouseX, mouseY, plusX, y, 28, ROW_HEIGHT - 1);
        graphics.fill(minusX, y, minusX + 28, y + ROW_HEIGHT - 1, hoverMinus ? COLOR_HOVER : COLOR_ROW);
        graphics.fill(plusX, y, plusX + 28, y + ROW_HEIGHT - 1, hoverPlus ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, "-", minusX + 14, y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
        graphics.centeredText(font, "+", plusX + 14, y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT);
        String v = String.valueOf(value);
        graphics.text(font, v, minusX - 10 - font.width(v),
            y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_MUTED, true);
    }

    private void applyStepper(MouseButtonEvent event, int panelX, int row, int value, int step,
            int min, int max, java.util.function.IntConsumer setter) {
        int y = rowTop(row);
        int minusX = panelX + PANEL_WIDTH - 96;
        int plusX = panelX + PANEL_WIDTH - 36;
        int delta = 0;
        if (isOver(event.x(), event.y(), minusX, y, 28, ROW_HEIGHT - 1)) delta = -step;
        if (isOver(event.x(), event.y(), plusX, y, 28, ROW_HEIGHT - 1)) delta = step;
        if (delta == 0) return;
        setter.accept(Mth.clamp(value + delta, min, max));
        MacroManager.save();
    }

    private void cycleRow(GuiGraphicsExtractor graphics, int panelX, int row, String label,
            String value, int mouseX, int mouseY) {
        int y = rowTop(row);
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.text(font, label, panelX + 22, y + (ROW_HEIGHT - font.lineHeight) / 2, COLOR_TEXT, true);
        graphics.text(font, value, panelX + PANEL_WIDTH - 22 - font.width(value),
            y + (ROW_HEIGHT - font.lineHeight) / 2, hovered ? COLOR_TEXT : COLOR_MUTED, true);
    }

    private boolean clickCycle(MouseButtonEvent event, int panelX, int row) {
        int y = rowTop(row);
        return isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
    }

    private void buttonRow(GuiGraphicsExtractor graphics, int panelX, int row, String label,
            int mouseX, int mouseY) {
        int y = rowTop(row);
        boolean hovered = isOver(mouseX, mouseY, panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
        graphics.fill(panelX + 16, y, panelX + PANEL_WIDTH - 16, y + ROW_HEIGHT - 1,
            hovered ? COLOR_HOVER : COLOR_ROW);
        graphics.centeredText(font, label, panelX + PANEL_WIDTH / 2,
            y + (ROW_HEIGHT - font.lineHeight) / 2, hovered ? COLOR_TEXT : COLOR_MUTED);
    }

    private boolean clickButton(MouseButtonEvent event, int panelX, int row) {
        int y = rowTop(row);
        return isOver(event.x(), event.y(), panelX + 16, y, PANEL_WIDTH - 32, ROW_HEIGHT - 1);
    }

    private void save() {
        if (task instanceof ChatTask chat && messageField != null) {
            chat.setMessage(messageField.getValue());
        }
        MacroManager.save();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private static boolean isOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
