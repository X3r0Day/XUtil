package me.x3r0day.xutil.client.repeater;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class ReplayEngine {

    private static Recording playing;
    private static int frameIndex;
    private static int remaining;
    private static boolean loop;
    private static boolean mouseLocked;

    private ReplayEngine() {
    }

    public static boolean isPlaying() {
        return playing != null;
    }

    public static boolean isMouseLocked() {
        return mouseLocked;
    }

    public static void setMouseLocked(boolean locked) {
        mouseLocked = locked;
    }

    public static void setLoop(boolean value) {
        loop = value;
    }

    public static void play(Recording recording) {
        playing = recording;
        frameIndex = 0;
        remaining = 0;
        mouseLocked = recording.isLockMouse();
    }

    public static void stop() {
        playing = null;
        mouseLocked = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            KeyBits.releaseAll(mc);
        }
    }

    public static void tick(Minecraft mc) {
        if (playing == null || mc.player == null) return;

        if (remaining <= 0) {
            if (frameIndex >= playing.getFrames().size()) {
                if (loop) {
                    frameIndex = 0;
                } else {
                    stop();
                    return;
                }
            } else {
                remaining = playing.getFrames().get(frameIndex).c();
            }
        }

        Recording.Frame frame = playing.getFrames().get(frameIndex);
        KeyBits.apply(mc, frame.k());
        mc.player.setYRot(mc.player.getYRot() + frame.dy());
        mc.player.setXRot(Math.max(-90f, Math.min(90f, mc.player.getXRot() + frame.dp())));

        int index = frame.h() - 1;
        if (index >= 0 && index <= 8 && mc.player.getInventory().getSelectedSlot() != index) {
            mc.player.getInventory().setSelectedSlot(index);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(index));
            }
        }

        remaining--;
        if (remaining <= 0) {
            frameIndex++;
        }
    }
}
