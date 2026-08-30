package me.x3r0day.xutil.client.macro;

import com.mojang.blaze3d.platform.InputConstants;
import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.MacroEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public final class Macro extends Module {

    public enum Trigger {
        KEYBIND("Keybind only"),
        ON_ENABLE("Once on enable"),
        EVERY_TICK("Every tick");

        private final String label;

        Trigger(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Trigger next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final List<MacroTask> tasks;
    private final MacroRun run;
    private Trigger trigger = Trigger.KEYBIND;
    private InputConstants.Key key = InputConstants.UNKNOWN;
    private boolean keyWasDown;

    public Macro(String name) {
        super(name, "A macro task chain. Bind a key to run it, or enable it to run automatically.",
            Category.MISC, InputConstants.UNKNOWN.getValue(), false);
        this.tasks = new ArrayList<>();
        this.run = new MacroRun(tasks);
    }

    public Macro(String name, List<MacroTask> tasks, Trigger trigger, InputConstants.Key key) {
        this(name);
        this.tasks.addAll(tasks);
        this.trigger = trigger;
        this.key = key;
    }

    public List<MacroTask> getTasks() {
        return tasks;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public InputConstants.Key getKey() {
        return key;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
        keyWasDown = false;
    }

    public boolean isKeyUnbound() {
        return key == InputConstants.UNKNOWN || key.getValue() == InputConstants.UNKNOWN.getValue();
    }

    public void tickKey(Minecraft mc) {
        boolean down = !isKeyUnbound() && InputConstants.isKeyDown(mc.getWindow(), key.getValue());
        if (down && !keyWasDown) {
            triggerRun();
        }
        keyWasDown = down;
    }

    public void triggerRun() {
        if (!run.isRunning()) {
            run.start();
        }
    }

    public void stopRun() {
        run.stop();
    }

    public void tickRun(Minecraft mc) {
        try {
            if (run.isRunning()) {
                run.tick(mc);
            } else if (isEnabled() && trigger == Trigger.EVERY_TICK) {
                run.start();
            }
        } catch (MacroBreakException e) {
            // BreakTask fired: stop the chain. For repeat triggers, disable
            // the macro too so it does not restart on the next tick.
            run.stop();
            if (trigger == Trigger.EVERY_TICK) {
                setEnabled(false);
            }
        }
    }

    @Override
    public boolean usesDefaultKeybindToggle() {
        return false;
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new MacroEditorScreen(this, parent));
        }
    }

    @Override
    protected void onEnable() {
        if (trigger == Trigger.ON_ENABLE) {
            run.start();
        }
    }

    @Override
    protected void onDisable() {
        run.stop();
    }
}
