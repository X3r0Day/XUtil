package me.x3r0day.xutil.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class KeybindConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("xutil-keybinds.json");

    private static final List<KeyMapping> EXTRA = new ArrayList<>();

    private KeybindConfig() {
    }

    public static void register(KeyMapping mapping) {
        EXTRA.add(mapping);
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            for (Module module : ModuleManager.getModules()) {
                KeyMapping keybind = module.getKeybind();
                if (keybind == null) {
                    continue;
                }
                String saved = GsonHelper.getAsString(root, keybind.getName(), "");
                if (!saved.isEmpty()) {
                    keybind.setKey(InputConstants.getKey(saved));
                }
            }
            for (KeyMapping mapping : EXTRA) {
                String saved = GsonHelper.getAsString(root, mapping.getName(), "");
                if (!saved.isEmpty()) {
                    mapping.setKey(InputConstants.getKey(saved));
                }
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.error("Failed to load keybinds", exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            KeyMapping keybind = module.getKeybind();
            if (keybind == null) {
                continue;
            }
            root.addProperty(keybind.getName(), keybind.saveString());
        }
        for (KeyMapping mapping : EXTRA) {
            root.addProperty(mapping.getName(), mapping.saveString());
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
