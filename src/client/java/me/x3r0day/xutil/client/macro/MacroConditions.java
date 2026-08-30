package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.condition.AlwaysCondition;
import me.x3r0day.xutil.client.macro.condition.BlockInFrontCondition;
import me.x3r0day.xutil.client.macro.condition.EntityHurtCondition;
import me.x3r0day.xutil.client.macro.condition.EntityInHitboxCondition;
import me.x3r0day.xutil.client.macro.condition.EntityInRangeCondition;
import me.x3r0day.xutil.client.macro.condition.HealthAboveCondition;
import me.x3r0day.xutil.client.macro.condition.HealthBelowCondition;
import me.x3r0day.xutil.client.macro.condition.HeldFoodCondition;
import me.x3r0day.xutil.client.macro.condition.HungerBelowCondition;
import me.x3r0day.xutil.client.macro.condition.InWaterCondition;
import me.x3r0day.xutil.client.macro.condition.OnGroundCondition;
import me.x3r0day.xutil.client.macro.condition.TimeOfDayCondition;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MacroConditions {

    public enum Group {
        PLAYER("Player"),
        WORLD("World"),
        ENTITY("Entity"),
        OTHER("Other");

        private final String label;

        Group(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Group next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public record ConditionType(String name, String description, Group group, Supplier<MacroCondition> factory) {
    }

    public static final List<ConditionType> TYPES = List.of(
        new ConditionType("Always", "Always true", Group.OTHER, AlwaysCondition::new),
        new ConditionType("Entity in range", "A living entity is within range", Group.ENTITY,
            () -> new EntityInRangeCondition(5)),
        new ConditionType("Entity in hitbox", "A living entity is under the crosshair in reach",
            Group.ENTITY, EntityInHitboxCondition::new),
        new ConditionType("Recently hurt", "The entity under the crosshair was hurt in the last moments",
            Group.ENTITY, EntityHurtCondition::new),
        new ConditionType("Health below", "Your health is below the value", Group.PLAYER,
            () -> new HealthBelowCondition(10)),
        new ConditionType("Health above", "Your health is above the value", Group.PLAYER,
            () -> new HealthAboveCondition(10)),
        new ConditionType("Hunger below", "Your hunger is below the value", Group.PLAYER,
            () -> new HungerBelowCondition(10)),
        new ConditionType("On ground", "You are standing on a block", Group.PLAYER, OnGroundCondition::new),
        new ConditionType("In water", "You are in water", Group.PLAYER, InWaterCondition::new),
        new ConditionType("Held is food", "The held item is food", Group.PLAYER, HeldFoodCondition::new),
        new ConditionType("Time of day", "The time of day matches", Group.WORLD,
            () -> new TimeOfDayCondition(TimeOfDayCondition.TimeOfDay.DAY)),
        new ConditionType("Block in front", "A solid block is a few blocks ahead", Group.WORLD,
            BlockInFrontCondition::new)
    );

    private static final Map<String, ConditionType> BY_TYPE = new HashMap<>();

    static {
        for (ConditionType type : TYPES) {
            BY_TYPE.put(type.factory().get().type(), type);
        }
    }

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
            case "health_above" -> new HealthAboveCondition(json.get("threshold").getAsDouble());
            case "hunger_below" -> new HungerBelowCondition(json.get("threshold").getAsInt());
            case "on_ground" -> new OnGroundCondition();
            case "in_water" -> new InWaterCondition();
            case "held_food" -> new HeldFoodCondition();
            case "time_of_day" -> new TimeOfDayCondition(
                safeTimeOfDay(GsonHelper.getAsString(json, "time", "DAY")));
            case "block_front" -> new BlockInFrontCondition();
            default -> throw new IllegalStateException("Unknown condition type: " + json.get("type").getAsString());
        };
    }

    public static List<ConditionType> byGroup(Group group) {
        List<ConditionType> out = new ArrayList<>();
        for (ConditionType type : TYPES) {
            if (type.group() == group) {
                out.add(type);
            }
        }
        return out;
    }

    public static ConditionType byType(MacroCondition condition) {
        return BY_TYPE.get(condition.type());
    }

    private static TimeOfDayCondition.TimeOfDay safeTimeOfDay(String value) {
        try {
            return TimeOfDayCondition.TimeOfDay.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TimeOfDayCondition.TimeOfDay.DAY;
        }
    }

    public static Group groupOf(MacroCondition condition) {
        ConditionType type = byType(condition);
        return type == null ? Group.OTHER : type.group();
    }
}
