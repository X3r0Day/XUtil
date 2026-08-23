package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import me.x3r0day.xutil.client.macro.task.ChatTask;
import me.x3r0day.xutil.client.macro.task.WaitTask;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MacroManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xutil-macros.json");

    private static final List<Macro> MACROS = new ArrayList<>();

    private MacroManager() {
    }

    public static void init() {
        load();
        ClientTickEvents.END_CLIENT_TICK.register(MacroManager::tick);
    }

    public static List<Macro> getMacros() {
        return Collections.unmodifiableList(MACROS);
    }

    public static Macro addMacro(String name) {
        Macro macro = new Macro(name);
        MACROS.add(macro);
        ModuleManager.register(macro);
        save();
        return macro;
    }

    public static void removeMacro(Macro macro) {
        macro.setEnabled(false);
        MACROS.remove(macro);
        ModuleManager.unregister(macro);
        save();
    }

    public static void save() {
        JsonArray array = new JsonArray();
        for (Macro macro : MACROS) {
            JsonObject json = new JsonObject();
            json.addProperty("name", macro.getName());
            json.addProperty("trigger", macro.getTrigger().name());
            json.addProperty("key", macro.getKey().getName());
            json.add("tasks", MacroTasks.toJsonArray(macro.getTasks()));
            array.add(json);
        }
        JsonObject root = new JsonObject();
        root.add("macros", array);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            seedDefaults();
            return;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            if (GsonHelper.isValidNode(root, "macros")) {
                loadNew(root);
            } else {
                migrateOld(root);
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.error("Failed to load macros", exception);
        }
    }

    private static void seedDefaults() {
        List<MacroTask> tasks = new ArrayList<>();
        tasks.add(new ChatTask("hello from xutil"));
        tasks.add(new WaitTask(20));
        Macro macro = new Macro("Example Macro", tasks, Macro.Trigger.KEYBIND, InputConstants.UNKNOWN);
        MACROS.add(macro);
        ModuleManager.register(macro);
        save();
    }

    private static void loadNew(JsonObject root) {
        JsonArray array = GsonHelper.getAsJsonArray(root, "macros", new JsonArray());
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            String name = GsonHelper.getAsString(json, "name", "");
            Macro.Trigger trigger = Macro.Trigger.valueOf(
                GsonHelper.getAsString(json, "trigger", "KEYBIND"));
            String keyName = GsonHelper.getAsString(json, "key", "");
            InputConstants.Key key = keyName.isEmpty() ? InputConstants.UNKNOWN : InputConstants.getKey(keyName);
            List<MacroTask> tasks = MacroTasks.fromJsonArray(
                GsonHelper.getAsJsonArray(json, "tasks", new JsonArray()));
            Macro macro = new Macro(name, tasks, trigger, key);
            MACROS.add(macro);
            ModuleManager.register(macro);
        }
        LOGGER.info("Loaded {} macros from {}", MACROS.size(), FILE.getFileName());
    }

    private static void migrateOld(JsonObject root) {
        for (String name : root.keySet()) {
            JsonObject entry = GsonHelper.getAsJsonObject(root, name);
            String message = GsonHelper.getAsString(entry, "message", "");
            String keyName = GsonHelper.getAsString(entry, "key", "");
            InputConstants.Key key = keyName.isEmpty() ? InputConstants.UNKNOWN : InputConstants.getKey(keyName);
            List<MacroTask> tasks = new ArrayList<>();
            tasks.add(new ChatTask(message));
            Macro macro = new Macro(name, tasks, Macro.Trigger.KEYBIND, key);
            MACROS.add(macro);
            ModuleManager.register(macro);
        }
        save();
        LOGGER.info("Migrated {} old macros from {}", MACROS.size(), FILE.getFileName());
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null) return;

        for (Macro macro : MACROS) {
            macro.tickKey(mc);
            macro.tickRun(mc);
        }
    }
}
