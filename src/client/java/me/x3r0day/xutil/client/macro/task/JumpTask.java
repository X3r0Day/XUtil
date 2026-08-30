package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class JumpTask extends MacroTask {

    private boolean pressed;

    @Override
    public String type() {
        return "jump";
    }

    @Override
    public String description() {
        return "Jump";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.options == null) return true;
        if (!pressed) {
            mc.options.keyJump.setDown(true);
            pressed = true;
            return false;
        }
        mc.options.keyJump.setDown(false);
        pressed = false;
        return true;
    }

    @Override
    public void reset() {
        pressed = false;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
