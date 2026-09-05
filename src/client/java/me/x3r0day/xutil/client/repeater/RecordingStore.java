package me.x3r0day.xutil.client.repeater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecordingStore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("xutil-recordings.json");

    private static final List<Recording> RECORDINGS = new ArrayList<>();

    private RecordingStore() {
    }

    public static void load() {
        RECORDINGS.clear();
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            JsonArray array = GsonHelper.getAsJsonArray(root, "recordings", new JsonArray());
            for (JsonElement element : array) {
                RECORDINGS.add(Recording.fromJson(element.getAsJsonObject()));
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.error("Failed to load recordings", exception);
        }
    }

    public static void save() {
        JsonArray array = new JsonArray();
        for (Recording recording : RECORDINGS) {
            array.add(recording.toJson());
        }
        JsonObject root = new JsonObject();
        root.add("recordings", array);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static List<Recording> getRecordings() {
        return Collections.unmodifiableList(RECORDINGS);
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (Recording recording : RECORDINGS) {
            names.add(recording.getName());
        }
        return names;
    }

    public static Recording getByName(String name) {
        for (Recording recording : RECORDINGS) {
            if (recording.getName().equals(name)) {
                return recording;
            }
        }
        return null;
    }

    public static void add(Recording recording) {
        RECORDINGS.add(recording);
        save();
    }

    public static void remove(String name) {
        RECORDINGS.removeIf(recording -> recording.getName().equals(name));
        save();
    }

    public static String suggestName() {
        int n = 1;
        while (getByName("Recording " + n) != null) {
            n++;
        }
        return "Recording " + n;
    }
}
