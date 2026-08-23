package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class AlwaysCondition extends MacroCondition {

    @Override
    public String type() {
        return "always";
    }

    @Override
    public String description() {
        return "always";
    }

    @Override
    public boolean test(Minecraft mc) {
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
