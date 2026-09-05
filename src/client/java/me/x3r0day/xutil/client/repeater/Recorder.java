package me.x3r0day.xutil.client.repeater;

import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Recorder {

    public static final int MAX_TICKS = 12000;

    private static Recording current;
    private static int ticks;
    private static float lastYaw;
    private static float lastPitch;
    private static int frameCount;
    private static float frameDy;
    private static float frameDp;
    private static int frameKeys;
    private static int frameSlot;
    private static List<String> frameModules = new ArrayList<>();
    private static boolean atCap;

    private static Map<String, Boolean> moduleSnapshot;
    private static Map<String, Boolean> lastModuleState;

    private Recorder() {
    }

    public static boolean isRecording() {
        return current != null;
    }

    public static boolean isAtCap() {
        return atCap;
    }

    public static int elapsedTicks() {
        return ticks;
    }

    public static void start(Minecraft mc) {
        current = new Recording(RecordingStore.suggestName(), new ArrayList<>(), false);
        current.setStart(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            mc.player.getYRot(), mc.player.getXRot());
        ticks = 0;
        atCap = false;
        lastYaw = mc.player.getYRot();
        lastPitch = mc.player.getXRot();
        frameCount = 0;
        frameDy = 0;
        frameDp = 0;
        frameKeys = KeyBits.capture(mc);
        frameSlot = mc.player.getInventory().getSelectedSlot() + 1;
        frameModules = new ArrayList<>();
        moduleSnapshot = snapshotModules();
        lastModuleState = snapshotModules();
    }

    public static Recording stop() {
        commit();
        Recording done = current;
        current = null;
        atCap = false;
        restoreModules();
        return done;
    }

    public static void discard() {
        current = null;
        atCap = false;
        restoreModules();
    }

    public static void capture(Minecraft mc) {
        if (current == null || mc.player == null) return;
        if (ticks >= MAX_TICKS) {
            atCap = true;
            return;
        }
        ticks++;

        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        float dy = quantize(wrapDegrees(yaw - lastYaw));
        float dp = quantize(pitch - lastPitch);
        lastYaw = yaw;
        lastPitch = pitch;

        int keys = KeyBits.capture(mc);
        int slot = mc.player.getInventory().getSelectedSlot() + 1;
        List<String> changed = diffModules();

        if (keys == frameKeys && slot == frameSlot && dy == frameDy && dp == frameDp
            && changed.isEmpty()) {
            frameCount++;
            return;
        }
        commit();
        frameCount = 1;
        frameDy = dy;
        frameDp = dp;
        frameKeys = keys;
        frameSlot = slot;
        frameModules = changed;
    }

    private static List<String> diffModules() {
        List<String> changed = new ArrayList<>();
        if (lastModuleState == null) return changed;
        for (Module module : ModuleManager.getModules()) {
            String name = module.getName();
            Boolean previous = lastModuleState.get(name);
            boolean now = module.isEnabled();
            if (previous == null) {
                lastModuleState.put(name, now);
            } else if (previous != now) {
                changed.add(name);
                lastModuleState.put(name, now);
            }
        }
        return changed;
    }

    private static void commit() {
        if (current == null || frameCount <= 0) return;
        current.getFrames().add(new Recording.Frame(frameCount, frameDy, frameDp, frameKeys, frameSlot,
            frameModules));
        frameCount = 0;
        frameModules = new ArrayList<>();
    }

    private static Map<String, Boolean> snapshotModules() {
        Map<String, Boolean> snapshot = new HashMap<>();
        for (Module module : ModuleManager.getModules()) {
            snapshot.put(module.getName(), module.isEnabled());
        }
        return snapshot;
    }

    private static void restoreModules() {
        if (moduleSnapshot == null) return;
        for (Module module : ModuleManager.getModules()) {
            Boolean saved = moduleSnapshot.get(module.getName());
            if (saved != null && module.isEnabled() != saved) {
                module.setEnabled(saved);
            }
        }
        moduleSnapshot = null;
        lastModuleState = null;
    }

    private static float quantize(float v) {
        return Math.round(v * 20f) / 20f;
    }

    private static float wrapDegrees(float d) {
        float w = (d + 540f) % 360f;
        return w - 180f;
    }
}
