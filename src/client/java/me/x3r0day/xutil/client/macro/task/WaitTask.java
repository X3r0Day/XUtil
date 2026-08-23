package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class WaitTask extends MacroTask {

    private int ticks;
    private int remaining = -1;

    public WaitTask(int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public String type() {
        return "wait";
    }

    @Override
    public String description() {
        return "Wait " + ticks + " ticks";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (remaining < 0) {
            remaining = ticks;
        }
        remaining--;
        if (remaining <= 0) {
            remaining = -1;
            return true;
        }
        return false;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("ticks", ticks);
    }
}
