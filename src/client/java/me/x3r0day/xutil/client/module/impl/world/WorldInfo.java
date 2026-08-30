package me.x3r0day.xutil.client.module.impl.world;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import me.x3r0day.xutil.client.ui.OptionListScreen.KeybindRow;
import me.x3r0day.xutil.client.ui.OptionToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Locale;

public class WorldInfo extends Module {

    private static final String[] DIRS = {"S", "W", "N", "E"};
    private static final int ENTITY_RANGE = 32;
    private static final int COLOR_TEXT = 0xFFCCCCCC;

    private static volatile boolean active;

    private static volatile boolean showBiome = true;
    private static volatile boolean showCoords = true;
    private static volatile boolean showDimension;
    private static volatile boolean showTime = true;
    private static volatile boolean showDay;
    private static volatile boolean showFacing = true;
    private static volatile boolean showLight;
    private static volatile boolean showFps;
    private static volatile boolean showEntities;

    public static final List<OptionToggle> TOGGLES = List.of(
        new OptionToggle("Biome", () -> showBiome, () -> showBiome = !showBiome),
        new OptionToggle("Coordinates", () -> showCoords, () -> showCoords = !showCoords),
        new OptionToggle("Dimension", () -> showDimension, () -> showDimension = !showDimension),
        new OptionToggle("Game time", () -> showTime, () -> showTime = !showTime),
        new OptionToggle("Day", () -> showDay, () -> showDay = !showDay),
        new OptionToggle("Facing", () -> showFacing, () -> showFacing = !showFacing),
        new OptionToggle("Light level", () -> showLight, () -> showLight = !showLight),
        new OptionToggle("FPS", () -> showFps, () -> showFps = !showFps),
        new OptionToggle("Nearby entities", () -> showEntities, () -> showEntities = !showEntities)
    );

    public WorldInfo() {
        super("WorldInfo", "Shows world info on screen. Right-click to pick what to show.", Category.WORLD);
    }

    public static boolean isActive() {
        return active;
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (!active || mc.player == null || mc.level == null) return;

        int x = 6;
        int y = 6;

        if (showBiome) {
            y = line(g, mc, x, y, "Biome", mc.level.getBiome(mc.player.blockPosition())
                .unwrapKey().map(key -> key.identifier().toString()).orElse("unknown"));
        }
        if (showCoords) {
            BlockPos pos = mc.player.blockPosition();
            y = line(g, mc, x, y, "XYZ",
                String.format(Locale.ROOT, "%d %d %d", pos.getX(), pos.getY(), pos.getZ()));
        }
        if (showDimension) {
            y = line(g, mc, x, y, "Dimension", mc.level.dimension().identifier().getPath());
        }
        if (showTime) {
            long t = mc.level.getOverworldClockTime() % 24000L;
            y = line(g, mc, x, y, "Time",
                String.format(Locale.ROOT, "%02d:%02d", (t / 1000 + 6) % 24, t % 1000 * 60 / 1000));
        }
        if (showDay) {
            y = line(g, mc, x, y, "Day", String.valueOf(mc.level.getOverworldClockTime() / 24000L + 1));
        }
        if (showFacing) {
            float yaw = Mth.wrapDegrees(mc.player.getYRot());
            int idx = Math.floorMod(Math.round(yaw / 90f), 4);
            y = line(g, mc, x, y, "Facing", DIRS[idx] + " " + (int) yaw + "\u00b0");
        }
        if (showLight) {
            int light = mc.level.getMaxLocalRawBrightness(mc.player.blockPosition());
            y = line(g, mc, x, y, "Light", light + "/15");
        }
        if (showFps) {
            y = line(g, mc, x, y, "FPS", String.valueOf(mc.getFps()));
        }
        if (showEntities) {
            y = line(g, mc, x, y, "Entities", String.valueOf(nearbyEntities(mc)));
        }
    }

    private static int line(GuiGraphicsExtractor g, Minecraft mc, int x, int y, String label, String value) {
        g.text(mc.font, label + ": " + value, x, y, COLOR_TEXT, true);
        return y + 10;
    }

    private static int nearbyEntities(Minecraft mc) {
        int px = (int) mc.player.getX();
        int pz = (int) mc.player.getZ();
        int count = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (Math.hypot(entity.getX() - px, entity.getZ() - pz) < ENTITY_RANGE) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new OptionListScreen("World Info",
                "Click a row to toggle what the overlay shows", TOGGLES,
                List.of(new KeybindRow("Keybind", getKeybind())), parent));
        }
    }

    @Override
    protected void onEnable() {
        active = true;
    }

    @Override
    protected void onDisable() {
        active = false;
    }
}
