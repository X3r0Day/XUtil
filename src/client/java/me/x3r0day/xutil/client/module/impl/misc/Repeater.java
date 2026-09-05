package me.x3r0day.xutil.client.module.impl.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.repeater.Recorder;
import me.x3r0day.xutil.client.repeater.Recording;
import me.x3r0day.xutil.client.repeater.RecordingStore;
import me.x3r0day.xutil.client.repeater.ReplayEngine;
import me.x3r0day.xutil.client.ui.GuiTheme;
import me.x3r0day.xutil.client.ui.RepeaterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class Repeater extends Module {

    private static volatile boolean loop;
    private static String selected = "";
    private static Repeater instance;

    public static InputConstants.Key recordKey = InputConstants.UNKNOWN;
    public static InputConstants.Key playKey = InputConstants.UNKNOWN;

    private boolean recordWasDown;
    private boolean playWasDown;

    public Repeater() {
        super("Recorder", "Records your inputs and replays them.", Category.MISC);
        instance = this;
    }

    public static String getSelected() {
        return selected;
    }

    public static void setSelected(String name) {
        selected = name;
    }

    public static boolean isLoop() {
        return loop;
    }

    public static void setLoop(boolean value) {
        loop = value;
        ReplayEngine.setLoop(value);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new RepeaterScreen(parent));
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null) return;

        boolean recordDown = keyDown(mc, recordKey);
        if (recordDown && !recordWasDown) {
            toggleRecording(mc);
        }
        recordWasDown = recordDown;

        boolean playDown = keyDown(mc, playKey);
        if (playDown && !playWasDown) {
            togglePlayback();
        }
        playWasDown = playDown;

        if (Recorder.isRecording()) {
            Recorder.capture(mc);
            if (Recorder.isAtCap()) {
                Recording done = Recorder.stop();
                if (done != null && !done.getFrames().isEmpty()) {
                    RecordingStore.add(done);
                    selected = done.getName();
                }
            }
        }
        if (ReplayEngine.isPlaying()) {
            ReplayEngine.tick(mc);
        }
    }

    private boolean keyDown(Minecraft mc, InputConstants.Key key) {
        if (key == null || key.getValue() == InputConstants.UNKNOWN.getValue()) return false;
        return InputConstants.isKeyDown(mc.getWindow(), key.getValue());
    }

    private void toggleRecording(Minecraft mc) {
        if (Recorder.isRecording()) {
            Recording done = Recorder.stop();
            if (done != null && !done.getFrames().isEmpty()) {
                RecordingStore.add(done);
                selected = done.getName();
            }
        } else {
            Recorder.start(mc);
        }
    }

    private void togglePlayback() {
        if (ReplayEngine.isPlaying()) {
            ReplayEngine.stop();
            return;
        }
        Recording recording = RecordingStore.getByName(selected);
        if (recording == null) {
            List<Recording> recordings = RecordingStore.getRecordings();
            if (!recordings.isEmpty()) {
                recording = recordings.get(recordings.size() - 1);
            }
        }
        if (recording != null) {
            ReplayEngine.play(recording);
        }
    }

    @Override
    protected void onDisable() {
        ReplayEngine.stop();
        Recorder.discard();
    }

    @Override
    protected void saveSettings(JsonObject json) {
        json.addProperty("loop", loop);
        json.addProperty("selected", selected);
        json.addProperty("record_key", recordKey.getName());
        json.addProperty("play_key", playKey.getName());
    }

    @Override
    protected void loadSettings(JsonObject json) {
        loop = GsonHelper.getAsBoolean(json, "loop", false);
        selected = GsonHelper.getAsString(json, "selected", "");
        recordKey = safeKey(GsonHelper.getAsString(json, "record_key", "key.keyboard.unknown"));
        playKey = safeKey(GsonHelper.getAsString(json, "play_key", "key.keyboard.unknown"));
        ReplayEngine.setLoop(loop);
    }

    private static InputConstants.Key safeKey(String name) {
        try {
            return InputConstants.getKey(name);
        } catch (RuntimeException e) {
            return InputConstants.UNKNOWN;
        }
    }

    public static void renderHud(GuiGraphicsExtractor graphics, Minecraft mc) {
        if (instance == null || mc.player == null || mc.font == null) return;

        String text;
        int color;
        if (Recorder.isRecording()) {
            text = String.format("REC 0:%04.1f", Recorder.elapsedTicks() / 20f);
            color = 0xFFFF5C5C;
        } else if (ReplayEngine.isPlaying()) {
            text = "PLAYING";
            color = GuiTheme.accent;
        } else {
            return;
        }
        graphics.centeredText(mc.font, text, mc.getWindow().getGuiScaledWidth() / 2, 6, color);
    }
}
