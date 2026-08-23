package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.condition.AlwaysCondition;
import me.x3r0day.xutil.client.macro.task.AttackTask;
import me.x3r0day.xutil.client.macro.task.ChatTask;
import me.x3r0day.xutil.client.macro.task.IfTask;
import me.x3r0day.xutil.client.macro.task.JumpTask;
import me.x3r0day.xutil.client.macro.task.LookTask;
import me.x3r0day.xutil.client.macro.task.LoopTask;
import me.x3r0day.xutil.client.macro.task.MoveTask;
import me.x3r0day.xutil.client.macro.task.ToggleModuleTask;
import me.x3r0day.xutil.client.macro.task.WaitTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MacroTasks {

    public record TaskType(String name, String description, Supplier<MacroTask> factory) {
    }

    public static final List<TaskType> TYPES = List.of(
        new TaskType("Chat", "Sends a chat message or command", () -> new ChatTask("")),
        new TaskType("Attack", "Attacks the entity under the crosshair", AttackTask::new),
        new TaskType("Wait", "Waits a number of ticks", () -> new WaitTask(20)),
        new TaskType("Walk", "Holds forward for a number of ticks", () -> new MoveTask(20)),
        new TaskType("Jump", "Jumps once", JumpTask::new),
        new TaskType("Turn", "Turns the player by yaw and pitch degrees", () -> new LookTask(0, 0)),
        new TaskType("Module", "Toggles a module on or off", () -> new ToggleModuleTask("", ToggleModuleTask.Action.TOGGLE)),
        new TaskType("If", "Runs tasks when a condition is true, otherwise runs other tasks",
            () -> new IfTask(new AlwaysCondition(), new ArrayList<>(), new ArrayList<>())),
        new TaskType("Loop", "Repeats tasks a number of times", () -> new LoopTask(3, new ArrayList<>()))
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

    public static MacroTask fromJson(JsonObject json) {
        return switch (json.get("type").getAsString()) {
            case "chat" -> new ChatTask(GsonHelper.getAsString(json, "message", ""));
            case "attack" -> new AttackTask();
            case "wait" -> new WaitTask(json.get("ticks").getAsInt());
            case "move" -> new MoveTask(json.get("ticks").getAsInt());
            case "jump" -> new JumpTask();
            case "look" -> new LookTask(json.get("yaw").getAsDouble(), json.get("pitch").getAsDouble());
            case "module" -> new ToggleModuleTask(
                GsonHelper.getAsString(json, "module", ""),
                ToggleModuleTask.Action.valueOf(GsonHelper.getAsString(json, "action", "TOGGLE")));
            case "if" -> new IfTask(
                MacroConditions.fromJson(json.getAsJsonObject("condition")),
                fromJsonArray(json.getAsJsonArray("then")),
                fromJsonArray(json.getAsJsonArray("else")));
            case "loop" -> new LoopTask(json.get("times").getAsInt(), fromJsonArray(json.getAsJsonArray("body")));
            default -> throw new IllegalStateException("Unknown task type: " + json.get("type").getAsString());
        };
    }
}
