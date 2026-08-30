package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class HungerBelowCondition extends MacroCondition {

    private int threshold;

    public HungerBelowCondition(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public String type() {
        return "hunger_below";
    }

    @Override
    public String description() {
        return "hunger below " + threshold;
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.player.getFoodData().getFoodLevel() < threshold;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("threshold", threshold);
    }
}
