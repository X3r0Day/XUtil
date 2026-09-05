package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import me.x3r0day.xutil.client.repeater.KeyBits;
import me.x3r0day.xutil.client.repeater.Recording;
import me.x3r0day.xutil.client.repeater.RecordingStore;
import me.x3r0day.xutil.client.repeater.ReplayEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.GsonHelper;

import java.util.HashSet;
import java.util.Set;

public final class ReplayTask extends MacroTask {

    private String recording = "";
    private Recording playing;
    private int frameIndex;
    private int remaining;
    private long lastTick = Long.MIN_VALUE;
    private final Set<String> toggledModules = new HashSet<>();

    public ReplayTask() {
    }

    public ReplayTask(String recording) {
        this.recording = recording;
    }

    public String getRecording() {
        return recording;
    }

    public void setRecording(String recording) {
        this.recording = recording;
    }

    @Override
    public String type() {
        return "replay";
    }

    @Override
    public String description() {
        return "Replay " + (recording.isEmpty() ? "<none>" : recording);
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return true;

        if (playing == null) {
            playing = RecordingStore.getByName(recording);
            if (playing == null) return true;
            frameIndex = 0;
            remaining = 0;
            lastTick = mc.level.getGameTime();
            ReplayEngine.setMouseLocked(playing.isLockMouse());
        }

        long now = mc.level.getGameTime();
        if (now == lastTick) return false;
        lastTick = now;

        if (remaining <= 0) {
            if (frameIndex >= playing.getFrames().size()) {
                finish(mc);
                return true;
            }
            Recording.Frame frame = playing.getFrames().get(frameIndex);
            if (frameIndex == 0) {
                mc.player.setPos(playing.getStartX(), playing.getStartY(), playing.getStartZ());
                mc.player.setYRot(playing.getStartYaw());
                mc.player.setXRot(playing.getStartPitch());
            }
            remaining = frame.c();
            for (String name : frame.modules()) {
                toggleModule(name);
                toggledModules.add(name);
            }
        }

        Recording.Frame frame = playing.getFrames().get(frameIndex);
        KeyBits.apply(mc, frame.k());
        mc.player.setYRot(mc.player.getYRot() + frame.dy());
        mc.player.setXRot(Math.max(-90f, Math.min(90f, mc.player.getXRot() + frame.dp())));
        applyHotbar(mc, frame.h());
        remaining--;
        if (remaining <= 0) {
            frameIndex++;
        }
        return false;
    }

    private void applyHotbar(Minecraft mc, int slot) {
        int index = slot - 1;
        if (index < 0 || index > 8) return;
        if (mc.player.getInventory().getSelectedSlot() == index) return;
        mc.player.getInventory().setSelectedSlot(index);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(index));
        }
    }

    private void finish(Minecraft mc) {
        KeyBits.releaseAll(mc);
        ReplayEngine.setMouseLocked(false);
        for (String name : toggledModules) {
            toggleModule(name);
        }
        toggledModules.clear();
        playing = null;
    }

    private void toggleModule(String name) {
        for (Module module : ModuleManager.getModules()) {
            if (module.getName().equals(name)) {
                module.toggle();
                return;
            }
        }
    }

    @Override
    public void reset() {
        if (playing != null) {
            finish(Minecraft.getInstance());
        }
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("recording", recording);
    }

    public static ReplayTask fromJson(JsonObject json) {
        return new ReplayTask(GsonHelper.getAsString(json, "recording", ""));
    }
}
