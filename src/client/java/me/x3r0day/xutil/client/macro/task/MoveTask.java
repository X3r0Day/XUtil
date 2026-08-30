package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class MoveTask extends MacroTask {

    private int ticks;
    private int remaining = -1;

    public MoveTask(int ticks) {
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
        return "move";
    }

    @Override
    public String description() {
        return "Walk forward " + ticks + " ticks";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (remaining < 0) {
            remaining = ticks;
            if (mc.options != null) {
                mc.options.keyUp.setDown(true);
            }
        }
        remaining--;
        if (remaining <= 0) {
            remaining = -1;
            if (mc.options != null) {
                mc.options.keyUp.setDown(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        remaining = -1;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("ticks", ticks);
    }
}
