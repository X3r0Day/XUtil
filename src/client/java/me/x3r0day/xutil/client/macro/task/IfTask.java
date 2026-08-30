package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroRun;
import me.x3r0day.xutil.client.macro.MacroStatement;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.macro.MacroTasks;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class IfTask extends MacroTask {

    private MacroStatement statement;
    private final List<MacroTask> thenTasks;
    private final List<MacroTask> elseTasks;

    private MacroRun branch;
    private boolean decided;

    public IfTask(MacroStatement statement, List<MacroTask> thenTasks, List<MacroTask> elseTasks) {
        this.statement = statement;
        this.thenTasks = thenTasks;
        this.elseTasks = elseTasks;
    }

    public MacroStatement getStatement() {
        return statement;
    }

    public void setStatement(MacroStatement statement) {
        this.statement = statement;
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
        return "If " + statement.description() + ": then " + thenTasks.size()
            + ", else " + elseTasks.size();
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (!decided) {
            decided = true;
            branch = new MacroRun(statement.test(mc) ? thenTasks : elseTasks);
            branch.start();
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
    public void reset() {
        decided = false;
        branch = null;
    }

    @Override
    public void toJson(JsonObject json) {
        json.add("condition", statement.toJson());
        json.add("then", MacroTasks.toJsonArray(thenTasks));
        json.add("else", MacroTasks.toJsonArray(elseTasks));
    }
}
