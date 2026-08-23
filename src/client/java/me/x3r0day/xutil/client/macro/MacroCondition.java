package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

public abstract class MacroCondition {

    public abstract String type();

    public abstract String description();

    public abstract boolean test(Minecraft mc);

    public abstract void toJson(JsonObject json);
}
