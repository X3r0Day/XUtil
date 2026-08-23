package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroRun;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.MacroTasks;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class LoopTask extends MacroTask {

    private int times;
    private final List<MacroTask> body;

    private int iteration = -1;
    private MacroRun bodyRun;

    public LoopTask(int times, List<MacroTask> body) {
        this.times = times;
        this.body = body;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
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
        return "Loop " + times + "x (" + body.size() + " tasks)";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (iteration < 0) {
            iteration = 0;
        }
        if (bodyRun == null) {
            bodyRun = new MacroRun(body);
        }
        bodyRun.tick(mc);
        if (!bodyRun.isRunning()) {
            bodyRun = null;
            iteration++;
            if (iteration >= times) {
                iteration = -1;
                return true;
            }
        }
        return false;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("times", times);
        json.add("body", MacroTasks.toJsonArray(body));
    }
}
