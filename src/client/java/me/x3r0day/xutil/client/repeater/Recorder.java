package me.x3r0day.xutil.client.repeater;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

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
    private static boolean atCap;

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
        ticks = 0;
        atCap = false;
        lastYaw = mc.player.getYRot();
        lastPitch = mc.player.getXRot();
        frameCount = 0;
        frameDy = 0;
        frameDp = 0;
        frameKeys = KeyBits.capture(mc);
        frameSlot = mc.player.getInventory().getSelectedSlot() + 1;
    }

    public static Recording stop() {
        commit();
        Recording done = current;
        current = null;
        atCap = false;
        return done;
    }

    public static void discard() {
        current = null;
        atCap = false;
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

        if (keys == frameKeys && slot == frameSlot && dy == frameDy && dp == frameDp) {
            frameCount++;
            return;
        }
        commit();
        frameCount = 1;
        frameDy = dy;
        frameDp = dp;
        frameKeys = keys;
        frameSlot = slot;
    }

    private static void commit() {
        if (current == null || frameCount <= 0) return;
        current.getFrames().add(new Recording.Frame(frameCount, frameDy, frameDp, frameKeys, frameSlot));
        frameCount = 0;
    }

    private static float quantize(float v) {
        return Math.round(v * 20f) / 20f;
    }

    private static float wrapDegrees(float d) {
        float w = (d + 540f) % 360f;
        return w - 180f;
    }
}
