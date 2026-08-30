package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class HealthAboveCondition extends MacroCondition {

    private double threshold;

    public HealthAboveCondition(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String type() {
        return "health_above";
    }

    @Override
    public String description() {
        return "health above " + (int) threshold;
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.player.getHealth() > threshold;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("threshold", threshold);
    }
}
