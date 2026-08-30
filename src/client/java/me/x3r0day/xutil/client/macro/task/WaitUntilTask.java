package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroStatement;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class WaitUntilTask extends MacroTask {

    private MacroStatement statement;

    public WaitUntilTask(MacroStatement statement) {
        this.statement = statement;
    }

    public MacroStatement getStatement() {
        return statement;
    }

    public void setStatement(MacroStatement statement) {
        this.statement = statement;
    }

    @Override
    public String type() {
        return "wait_until";
    }

    @Override
    public String description() {
        return "Wait until " + statement.description();
    }

    @Override
    public boolean tick(Minecraft mc) {
        return statement.test(mc);
    }

    @Override
    public void toJson(JsonObject json) {
        json.add("condition", statement.toJson());
    }
}
