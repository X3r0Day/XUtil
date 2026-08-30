package me.x3r0day.xutil.client.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GuiTheme {

    public static final int[] PALETTE = {
        0xFF8A5CFF, 0xFF5C8AFF, 0xFF5CFFB8, 0xFFFFE05C,
        0xFFFF8A5C, 0xFFFF5C5C, 0xFFFF5CD8, 0xFFFFFFFF
    };

    public static int accent = PALETTE[0];

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xutil-theme.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GuiTheme() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            accent = GsonHelper.getAsInt(root, "accent", accent);
        } catch (RuntimeException | IOException ignored) {
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("accent", accent);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void next() {
        for (int i = 0; i < PALETTE.length; i++) {
            if (PALETTE[i] == accent) {
                accent = PALETTE[(i + 1) % PALETTE.length];
                save();
                return;
            }
        }
        accent = PALETTE[0];
        save();
    }
}
