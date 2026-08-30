package me.x3r0day.xutil.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ModuleConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("xutil-modules.json");
    private static final Path WINDOW_FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("xutil-windows.json");

    private ModuleConfig() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            int restored = 0;
            for (Module module : ModuleManager.getModules()) {
                String name = module.getName();
                if (GsonHelper.isBooleanValue(root, name) && GsonHelper.getAsBoolean(root, name)) {
                    module.setEnabled(true);
                    restored++;
                }
            }
            LOGGER.info("Restored {} enabled modules from {}", restored, FILE.getFileName());
        } catch (RuntimeException | IOException exception) {
            LOGGER.error("Failed to load module states", exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            root.addProperty(module.getName(), module.isEnabled());
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static Map<String, int[]> loadWindowPositions() {
        Map<String, int[]> positions = new HashMap<>();
        if (!Files.exists(WINDOW_FILE)) {
            return positions;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(WINDOW_FILE, StandardCharsets.UTF_8));
            for (String categoryName : root.keySet()) {
                JsonArray pos = GsonHelper.getAsJsonArray(root, categoryName);
                if (pos.size() == 2) {
                    positions.put(categoryName,
                        new int[]{pos.get(0).getAsInt(), pos.get(1).getAsInt()});
                }
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.error("Failed to load window positions", exception);
        }
        return positions;
    }

    public static void saveWindowPositions(Map<String, int[]> positions) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, int[]> entry : positions.entrySet()) {
            JsonArray pos = new JsonArray();
            pos.add(entry.getValue()[0]);
            pos.add(entry.getValue()[1]);
            root.add(entry.getKey(), pos);
        }
        try {
            Files.createDirectories(WINDOW_FILE.getParent());
            Files.writeString(WINDOW_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
