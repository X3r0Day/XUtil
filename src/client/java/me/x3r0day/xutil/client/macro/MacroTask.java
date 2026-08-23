package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

public abstract class MacroTask {

    public abstract String type();

    public abstract String description();

    /** Returns true when the task is finished and the chain should advance. */
    public abstract boolean tick(Minecraft mc);

    public abstract void toJson(JsonObject json);
}
