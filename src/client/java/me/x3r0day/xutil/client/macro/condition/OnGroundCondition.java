package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class OnGroundCondition extends MacroCondition {

    @Override
    public String type() {
        return "on_ground";
    }

    @Override
    public String description() {
        return "you are standing on a block";
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.player.onGround();
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
