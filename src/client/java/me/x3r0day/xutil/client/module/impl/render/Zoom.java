package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import me.x3r0day.xutil.client.ui.OptionListScreen.KeybindRow;
import me.x3r0day.xutil.client.ui.OptionToggle;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import com.google.gson.JsonObject;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class Zoom extends Module {

    private static final double DEFAULT_ZOOM = 6.0;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 50.0;
    private static final double SMOOTH_SPEED = 5.0;

    private static volatile boolean active;
    private static volatile boolean holdMode = true;
    private static volatile boolean smooth = true;
    private static volatile boolean scrollZoom = true;

    private static Zoom instance;
    private static KeyMapping keybind;
    private static double zoomValue = DEFAULT_ZOOM;
    private static double preSensitivity = 0.5;
    private static boolean sensitivityRestored = true;
    private static double time;
    private static long lastFrameNanos;

    public static final List<OptionToggle> TOGGLES = List.of(
        new OptionToggle("Scroll to zoom", () -> scrollZoom, () -> scrollZoom = !scrollZoom),
        new OptionToggle("Smooth zoom", () -> smooth, () -> smooth = !smooth),
        new OptionToggle("Toggle", () -> !holdMode, () -> holdMode = !holdMode)
    );

    public Zoom() {
        super("Zoom", "Hold the keybind to zoom, or enable Toggle to press it once.",
            Category.RENDER, GLFW.GLFW_KEY_C);
        keybind = getKeybind();
        instance = this;
        ClientTickEvents.END_CLIENT_TICK.register(Zoom::tickKey);
    }

    @Override
    protected void saveSettings(JsonObject json) {
        json.addProperty("hold", holdMode);
        json.addProperty("smooth", smooth);
        json.addProperty("scroll", scrollZoom);
    }

    @Override
    protected void loadSettings(JsonObject json) {
        holdMode = GsonHelper.getAsBoolean(json, "hold", true);
        smooth = GsonHelper.getAsBoolean(json, "smooth", true);
        scrollZoom = GsonHelper.getAsBoolean(json, "scroll", true);
    }

    @Override
    public boolean usesDefaultKeybindToggle() {
        return !holdMode;
    }

    private static void tickKey(Minecraft mc) {
        if (instance == null || mc.options == null || keybind == null || !holdMode) return;
        boolean down = keybind.isDown();
        if (down != instance.isEnabled()) {
            instance.setEnabled(down);
        }
    }

    @Override
    protected void onEnable() {
        active = true;
        time = 0.001;
        lastFrameNanos = 0;
        zoomValue = DEFAULT_ZOOM;
        preSensitivity = Minecraft.getInstance().options.sensitivity().get();
        sensitivityRestored = false;
        while (keybind.consumeClick()) {
        }
    }

    @Override
    protected void onDisable() {
        active = false;
        while (keybind.consumeClick()) {
        }
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new OptionListScreen("Zoom", "Click a row to change behavior",
                TOGGLES, List.of(new KeybindRow("Keybind", keybind)), parent));
        }
    }

    public static void frame(Minecraft mc) {
        if (mc.options == null) return;

        long now = System.nanoTime();
        double dt = lastFrameNanos == 0 ? 0 : (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;

        if (smooth) {
            time += (active ? 1 : -1) * dt * SMOOTH_SPEED;
            time = Mth.clamp(time, 0, 1);
        } else {
            time = active ? 1 : 0;
        }

        if (time > 0) {
            mc.options.sensitivity().set(preSensitivity / Math.max(getScaling() * 0.5, 1));
        } else if (!sensitivityRestored) {
            mc.options.sensitivity().set(preSensitivity);
            sensitivityRestored = true;
        }
    }

    public static double getScaling() {
        double delta = time < 0.5 ? 4 * time * time * time : 1 - Math.pow(-2 * time + 2, 3) / 2;
        return Mth.lerp(delta, 1, zoomValue);
    }

    public static double getFovMultiplier() {
        return 1.0 / getScaling();
    }

    public static boolean isZooming() {
        return active;
    }

    public static boolean onScrollTicks(int ticks) {
        if (!active || !scrollZoom) return false;
        if (Minecraft.getInstance().gui.screen() != null) return false;
        zoomValue = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomValue + ticks * 0.25 * zoomValue));
        return true;
    }
}
