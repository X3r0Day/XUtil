package me.x3r0day.xutil.client.macro;

import net.minecraft.client.Minecraft;

import java.util.List;

public final class MacroRun {

    private final List<MacroTask> tasks;
    private int index;
    private boolean running;

    public MacroRun(List<MacroTask> tasks) {
        this.tasks = tasks;
    }

    public void start() {
        index = 0;
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void tick(Minecraft mc) {
        if (!running) return;
        if (index >= tasks.size()) {
            running = false;
            return;
        }
        if (tasks.get(index).tick(mc)) {
            index++;
            if (index >= tasks.size()) {
                running = false;
            }
        }
    }
}
