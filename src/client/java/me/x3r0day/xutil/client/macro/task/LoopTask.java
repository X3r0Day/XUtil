package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroRun;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.MacroTasks;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class LoopTask extends MacroTask {

    private int times;
    private boolean infinite;
    private final List<MacroTask> body;

    private int iteration = -1;
    private MacroRun bodyRun;

    public LoopTask(int times, List<MacroTask> body) {
        this(times, false, body);
    }

    public LoopTask(int times, boolean infinite, List<MacroTask> body) {
        this.times = times;
        this.infinite = infinite;
        this.body = body;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public List<MacroTask> getBody() {
        return body;
    }

    @Override
    public String type() {
        return "loop";
    }

    @Override
    public String description() {
        return infinite ? "Loop forever (" + body.size() + " tasks)"
            : "Loop " + times + "x (" + body.size() + " tasks)";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (iteration < 0) {
            iteration = 0;
        }
        // finish whole iterations in one tick while the body is instant
        int guard = 0;
        while (MacroRun.maxStepsPerTick <= 0 || guard < MacroRun.maxStepsPerTick) {
            if (bodyRun == null) {
                bodyRun = new MacroRun(body);
                bodyRun.start();
            }
            bodyRun.tick(mc);
            if (bodyRun.isRunning()) {
                return false;
            }
            bodyRun = null;
            iteration++;
            guard++;
            if (!infinite && iteration >= times) {
                iteration = -1;
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        iteration = -1;
        bodyRun = null;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("times", times);
        json.addProperty("infinite", infinite);
        json.add("body", MacroTasks.toJsonArray(body));
    }
}
