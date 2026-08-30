package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroBreakException;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class BreakTask extends MacroTask {

    @Override
    public String type() {
        return "break";
    }

    @Override
    public String description() {
        return "Stop the whole macro chain";
    }

    @Override
    public boolean tick(Minecraft mc) {
        throw new MacroBreakException();
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
