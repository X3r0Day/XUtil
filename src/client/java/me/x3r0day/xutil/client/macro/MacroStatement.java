package me.x3r0day.xutil.client.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public final class MacroStatement {

    public enum Operator {
        ALL("Match all"),
        ANY("Match any");

        private final String label;

        Operator(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Operator next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public record Part(MacroCondition condition, boolean negate) {
    }

    private Operator operator;
    private final List<Part> parts = new ArrayList<>();

    public MacroStatement(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void add(MacroCondition condition, boolean negate) {
        parts.add(new Part(condition, negate));
    }

    public void replace(int index, MacroCondition condition) {
        parts.set(index, new Part(condition, parts.get(index).negate()));
    }

    public void remove(int index) {
        parts.remove(index);
    }

    public boolean test(Minecraft mc) {
        if (operator == Operator.ALL) {
            for (Part part : parts) {
                if (!matches(part, mc)) {
                    return false;
                }
            }
            return true;
        }
        for (Part part : parts) {
            if (matches(part, mc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Part part, Minecraft mc) {
        boolean result = part.condition().test(mc);
        return part.negate() ? !result : result;
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(operator == Operator.ALL ? " and " : " or ");
            }
            Part part = parts.get(i);
            if (part.negate()) {
                sb.append("not ");
            }
            sb.append(part.condition().description());
        }
        return sb.toString();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("op", operator.name());
        JsonArray array = new JsonArray();
        for (Part part : parts) {
            JsonObject entry = new JsonObject();
            entry.add("cond", MacroConditions.toJson(part.condition()));
            entry.addProperty("negate", part.negate());
            array.add(entry);
        }
        json.add("parts", array);
        return json;
    }

    public static MacroStatement single(MacroCondition condition) {
        MacroStatement statement = new MacroStatement(Operator.ALL);
        statement.add(condition, false);
        return statement;
    }

    public static MacroStatement fromJson(JsonObject json) {
        if (json.has("op")) {
            Operator operator = Operator.ALL;
            try {
                operator = Operator.valueOf(GsonHelper.getAsString(json, "op", "ALL").toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
            MacroStatement statement = new MacroStatement(operator);
            JsonArray array = GsonHelper.getAsJsonArray(json, "parts", new JsonArray());
            for (JsonElement element : array) {
                JsonObject entry = element.getAsJsonObject();
                statement.add(
                    MacroConditions.fromJson(entry.getAsJsonObject("cond")),
                    GsonHelper.getAsBoolean(entry, "negate", false));
            }
            return statement;
        }
        // old single-condition format
        MacroStatement statement = new MacroStatement(Operator.ALL);
        statement.add(MacroConditions.fromJson(json), false);
        return statement;
    }
}
