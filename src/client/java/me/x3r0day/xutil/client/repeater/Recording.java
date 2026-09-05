package me.x3r0day.xutil.client.repeater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public final class Recording {

    public record Frame(int c, float dy, float dp, int k, int h, List<String> modules) {

        public Frame(int c, float dy, float dp, int k, int h) {
            this(c, dy, dp, k, h, List.of());
        }
    }

    private String name;
    private final List<Frame> frames = new ArrayList<>();
    private boolean lockMouse;
    private double startX;
    private double startY;
    private double startZ;
    private float startYaw;
    private float startPitch;

    public Recording(String name, List<Frame> frames, boolean lockMouse) {
        this.name = name;
        this.frames.addAll(frames);
        this.lockMouse = lockMouse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public boolean isLockMouse() {
        return lockMouse;
    }

    public void setLockMouse(boolean lockMouse) {
        this.lockMouse = lockMouse;
    }

    public void setStart(double x, double y, double z, float yaw, float pitch) {
        startX = x;
        startY = y;
        startZ = z;
        startYaw = yaw;
        startPitch = pitch;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    public float getStartYaw() {
        return startYaw;
    }

    public float getStartPitch() {
        return startPitch;
    }

    public float duration() {
        int total = 0;
        for (Frame frame : frames) {
            total += frame.c();
        }
        return total / 20f;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("lock_mouse", lockMouse);
        json.addProperty("sx", startX);
        json.addProperty("sy", startY);
        json.addProperty("sz", startZ);
        json.addProperty("syaw", startYaw);
        json.addProperty("spitch", startPitch);
        JsonArray array = new JsonArray();
        for (Frame frame : frames) {
            JsonObject entry = new JsonObject();
            entry.addProperty("c", frame.c());
            entry.addProperty("dy", frame.dy());
            entry.addProperty("dp", frame.dp());
            entry.addProperty("k", frame.k());
            entry.addProperty("h", frame.h());
            if (!frame.modules().isEmpty()) {
                JsonArray modules = new JsonArray();
                for (String module : frame.modules()) {
                    modules.add(module);
                }
                entry.add("m", modules);
            }
            array.add(entry);
        }
        json.add("frames", array);
        return json;
    }

    public static Recording fromJson(JsonObject json) {
        List<Frame> frames = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(json, "frames", new JsonArray());
        for (JsonElement element : array) {
            JsonObject entry = element.getAsJsonObject();
            List<String> modules = new ArrayList<>();
            JsonArray moduleArray = GsonHelper.getAsJsonArray(entry, "m", new JsonArray());
            for (JsonElement module : moduleArray) {
                modules.add(module.getAsString());
            }
            frames.add(new Frame(
                GsonHelper.getAsInt(entry, "c", 1),
                GsonHelper.getAsFloat(entry, "dy", 0f),
                GsonHelper.getAsFloat(entry, "dp", 0f),
                GsonHelper.getAsInt(entry, "k", 0),
                GsonHelper.getAsInt(entry, "h", 1),
                modules));
        }
        Recording recording = new Recording(GsonHelper.getAsString(json, "name", "Recording"),
            frames, GsonHelper.getAsBoolean(json, "lock_mouse", false));
        recording.setStart(
            GsonHelper.getAsDouble(json, "sx", 0),
            GsonHelper.getAsDouble(json, "sy", 0),
            GsonHelper.getAsDouble(json, "sz", 0),
            GsonHelper.getAsFloat(json, "syaw", 0f),
            GsonHelper.getAsFloat(json, "spitch", 0f));
        return recording;
    }
}
