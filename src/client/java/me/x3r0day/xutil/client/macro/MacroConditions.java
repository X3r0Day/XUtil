package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.condition.AlwaysCondition;
import me.x3r0day.xutil.client.macro.condition.EntityHurtCondition;
import me.x3r0day.xutil.client.macro.condition.EntityInHitboxCondition;
import me.x3r0day.xutil.client.macro.condition.EntityInRangeCondition;
import me.x3r0day.xutil.client.macro.condition.HealthBelowCondition;

import java.util.List;
import java.util.function.Supplier;

public final class MacroConditions {

    public record ConditionType(String name, String description, Supplier<MacroCondition> factory) {
    }

    public static final List<ConditionType> TYPES = List.of(
        new ConditionType("Always", "Always true", AlwaysCondition::new),
        new ConditionType("Entity in range", "A living entity is within range", () -> new EntityInRangeCondition(5)),
        new ConditionType("Entity in hitbox", "A living entity is under the crosshair in reach",
            EntityInHitboxCondition::new),
        new ConditionType("Recently hurt", "The entity under the crosshair was hurt in the last moments",
            EntityHurtCondition::new),
        new ConditionType("Health below", "Your health is below the value", () -> new HealthBelowCondition(10))
    );

    private MacroConditions() {
    }

    public static JsonObject toJson(MacroCondition condition) {
        JsonObject json = new JsonObject();
        json.addProperty("type", condition.type());
        condition.toJson(json);
        return json;
    }

    public static MacroCondition fromJson(JsonObject json) {
        return switch (json.get("type").getAsString()) {
            case "always" -> new AlwaysCondition();
            case "entity_range" -> new EntityInRangeCondition(json.get("radius").getAsDouble());
            case "entity_hitbox" -> new EntityInHitboxCondition();
            case "entity_hurt" -> new EntityHurtCondition();
            case "health_below" -> new HealthBelowCondition(json.get("threshold").getAsDouble());
            default -> throw new IllegalStateException("Unknown condition type: " + json.get("type").getAsString());
        };
    }

    public static int indexOf(MacroCondition condition) {
        return switch (condition.type()) {
            case "always" -> 0;
            case "entity_range" -> 1;
            case "entity_hitbox" -> 2;
            case "entity_hurt" -> 3;
            case "health_below" -> 4;
            default -> -1;
        };
    }
}
