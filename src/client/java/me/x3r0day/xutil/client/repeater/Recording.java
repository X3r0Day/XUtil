package me.x3r0day.xutil.client.repeater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public final class Recording {

    public record Frame(int c, float dy, float dp, int k, int h) {
    }

    private String name;
    private final List<Frame> frames = new ArrayList<>();
    private boolean lockMouse;

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
        JsonArray array = new JsonArray();
        for (Frame frame : frames) {
            JsonObject entry = new JsonObject();
            entry.addProperty("c", frame.c());
            entry.addProperty("dy", frame.dy());
            entry.addProperty("dp", frame.dp());
            entry.addProperty("k", frame.k());
            entry.addProperty("h", frame.h());
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
            frames.add(new Frame(
                GsonHelper.getAsInt(entry, "c", 1),
                GsonHelper.getAsFloat(entry, "dy", 0f),
                GsonHelper.getAsFloat(entry, "dp", 0f),
                GsonHelper.getAsInt(entry, "k", 0),
                GsonHelper.getAsInt(entry, "h", 1)));
        }
        return new Recording(GsonHelper.getAsString(json, "name", "Recording"),
            frames, GsonHelper.getAsBoolean(json, "lock_mouse", false));
    }
}
