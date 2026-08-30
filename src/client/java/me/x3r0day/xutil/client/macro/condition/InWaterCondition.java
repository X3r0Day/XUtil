package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class InWaterCondition extends MacroCondition {

    @Override
    public String type() {
        return "in_water";
    }

    @Override
    public String description() {
        return "you are in water";
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.player.isInWater();
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
