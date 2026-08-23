package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import me.x3r0day.xutil.client.macro.MacroConditions;
import me.x3r0day.xutil.client.macro.MacroRun;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.MacroTasks;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class IfTask extends MacroTask {

    private MacroCondition condition;
    private final List<MacroTask> thenTasks;
    private final List<MacroTask> elseTasks;

    private MacroRun branch;
    private boolean decided;

    public IfTask(MacroCondition condition, List<MacroTask> thenTasks, List<MacroTask> elseTasks) {
        this.condition = condition;
        this.thenTasks = thenTasks;
        this.elseTasks = elseTasks;
    }

    public MacroCondition getCondition() {
        return condition;
    }

    public void setCondition(MacroCondition condition) {
        this.condition = condition;
    }

    public List<MacroTask> getThenTasks() {
        return thenTasks;
    }

    public List<MacroTask> getElseTasks() {
        return elseTasks;
    }

    @Override
    public String type() {
        return "if";
    }

    @Override
    public String description() {
        return "If " + condition.description() + ": then " + thenTasks.size()
            + ", else " + elseTasks.size();
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (!decided) {
            decided = true;
            branch = new MacroRun(condition.test(mc) ? thenTasks : elseTasks);
        }
        branch.tick(mc);
        if (!branch.isRunning()) {
            decided = false;
            branch = null;
            return true;
        }
        return false;
    }

    @Override
    public void toJson(JsonObject json) {
        json.add("condition", MacroConditions.toJson(condition));
        json.add("then", MacroTasks.toJsonArray(thenTasks));
        json.add("else", MacroTasks.toJsonArray(elseTasks));
    }
}
