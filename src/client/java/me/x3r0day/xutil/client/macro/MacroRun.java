package me.x3r0day.xutil.client.macro;

import net.minecraft.client.Minecraft;

import java.util.List;

public final class MacroRun {

    // max instant tasks per tick, 0 or less means no limit
    public static int maxStepsPerTick = 64;

    private final List<MacroTask> tasks;
    private int index;
    private boolean running;

    public MacroRun(List<MacroTask> tasks) {
        this.tasks = tasks;
    }

    public void start() {
        index = 0;
        running = true;
        for (MacroTask task : tasks) {
            task.reset();
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void tick(Minecraft mc) {
        if (!running) return;
        // quick tasks all finish in the same tick, a 3 step chain shouldn't take 3 ticks
        int guard = 0;
        try {
            while (running && index < tasks.size()
                && (maxStepsPerTick <= 0 || guard < maxStepsPerTick)) {
                if (!tasks.get(index).tick(mc)) {
                    break;
                }
                index++;
                guard++;
            }
            if (running && index >= tasks.size()) {
                running = false;
            }
        } catch (MacroBreakException e) {
            running = false;
            throw e;
        }
    }
}
