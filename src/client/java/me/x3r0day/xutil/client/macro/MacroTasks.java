package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.condition.AlwaysCondition;
import me.x3r0day.xutil.client.macro.task.AttackTask;
import me.x3r0day.xutil.client.macro.task.BreakTask;
import me.x3r0day.xutil.client.macro.task.ChatTask;
import me.x3r0day.xutil.client.macro.task.IfTask;
import me.x3r0day.xutil.client.macro.task.HotbarTask;
import me.x3r0day.xutil.client.macro.task.JumpTask;
import me.x3r0day.xutil.client.macro.task.LookTask;
import me.x3r0day.xutil.client.macro.task.LoopTask;
import me.x3r0day.xutil.client.macro.task.MoveTask;
import me.x3r0day.xutil.client.macro.task.ReplayTask;
import me.x3r0day.xutil.client.macro.task.ToggleModuleTask;
import me.x3r0day.xutil.client.macro.task.UseTask;
import me.x3r0day.xutil.client.macro.task.WaitTask;
import me.x3r0day.xutil.client.macro.task.WaitUntilTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MacroTasks {

    public record TaskType(String name, String description, Supplier<MacroTask> factory) {
    }

    public static final List<TaskType> TYPES = List.of(
        new TaskType("Chat", "Sends a chat message or command", () -> new ChatTask("")),
        new TaskType("Attack", "Attacks by crosshair, nearest or all entities", AttackTask::new),
        new TaskType("Wait", "Waits a number of ticks", () -> new WaitTask(20)),
        new TaskType("Wait until", "Waits until a condition is true",
            () -> new WaitUntilTask(MacroStatement.single(new AlwaysCondition()))),
        new TaskType("Use", "Uses the held item", UseTask::new),
        new TaskType("Hotbar", "Selects a hotbar slot", () -> new HotbarTask(1)),
        new TaskType("Walk", "Holds forward for a number of ticks", () -> new MoveTask(20)),
        new TaskType("Jump", "Jumps once", JumpTask::new),
        new TaskType("Replay", "Replays a saved recording", ReplayTask::new),
        new TaskType("Turn", "Turns by yaw and pitch, or aims at a target", LookTask::new),
        new TaskType("Module", "Toggles a module on or off", () -> new ToggleModuleTask("", ToggleModuleTask.Action.TOGGLE)),
        new TaskType("If", "Runs tasks when a condition is true, otherwise runs other tasks",
            () -> new IfTask(MacroStatement.single(new AlwaysCondition()),
                new ArrayList<>(), new ArrayList<>())),
        new TaskType("Loop", "Repeats tasks a number of times, or forever",
            () -> new LoopTask(3, new ArrayList<>())),
        new TaskType("Break", "Stops the whole macro chain", BreakTask::new)
    );

    private MacroTasks() {
    }

    public static JsonObject toJson(MacroTask task) {
        JsonObject json = new JsonObject();
        json.addProperty("type", task.type());
        task.toJson(json);
        return json;
    }

    public static JsonArray toJsonArray(List<MacroTask> tasks) {
        JsonArray array = new JsonArray();
        for (MacroTask task : tasks) {
            array.add(toJson(task));
        }
        return array;
    }

    public static List<MacroTask> fromJsonArray(JsonArray array) {
        List<MacroTask> tasks = new ArrayList<>();
        for (JsonElement element : array) {
            tasks.add(fromJson(element.getAsJsonObject()));
        }
        return tasks;
    }

    private static ToggleModuleTask.Action safeAction(String value) {
        try {
            return ToggleModuleTask.Action.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ToggleModuleTask.Action.TOGGLE;
        }
    }

    public static MacroTask fromJson(JsonObject json) {
        return switch (json.get("type").getAsString()) {
            case "chat" -> new ChatTask(GsonHelper.getAsString(json, "message", ""));
            case "attack" -> AttackTask.fromJson(json);
            case "wait" -> new WaitTask(json.get("ticks").getAsInt());
            case "wait_until" -> new WaitUntilTask(
                MacroStatement.fromJson(json.getAsJsonObject("condition")));
            case "use" -> new UseTask();
            case "hotbar" -> new HotbarTask(json.get("slot").getAsInt());
            case "move" -> new MoveTask(json.get("ticks").getAsInt());
            case "jump" -> new JumpTask();
            case "replay" -> ReplayTask.fromJson(json);
            case "look" -> LookTask.fromJson(json);
            case "module" -> new ToggleModuleTask(
                GsonHelper.getAsString(json, "module", ""),
                safeAction(GsonHelper.getAsString(json, "action", "TOGGLE")));
            case "if" -> new IfTask(
                MacroStatement.fromJson(json.getAsJsonObject("condition")),
                fromJsonArray(json.getAsJsonArray("then")),
                fromJsonArray(json.getAsJsonArray("else")));
            case "loop" -> new LoopTask(
                json.get("times").getAsInt(),
                GsonHelper.getAsBoolean(json, "infinite", false),
                fromJsonArray(json.getAsJsonArray("body")));
            case "break" -> new BreakTask();
            default -> throw new IllegalStateException("Unknown task type: " + json.get("type").getAsString());
        };
    }
}
