package me.x3r0day.xutil.client.module;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModuleConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("xutil-modules.json");

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
            Files.writeString(FILE, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
