package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class LookTask extends MacroTask {

    private double yaw;
    private double pitch;

    public LookTask(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    @Override
    public String type() {
        return "look";
    }

    @Override
    public String description() {
        return "Turn yaw " + format(yaw) + ", pitch " + format(pitch);
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player != null) {
            mc.player.turn(yaw, pitch);
        }
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
    }

    private static String format(double value) {
        return String.valueOf((int) value);
    }
}
