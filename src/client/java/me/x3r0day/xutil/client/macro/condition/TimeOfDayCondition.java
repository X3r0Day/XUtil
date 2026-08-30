package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;

public final class TimeOfDayCondition extends MacroCondition {

    public enum TimeOfDay {
        DAY("Day"),
        NIGHT("Night");

        private final String label;

        TimeOfDay(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public TimeOfDay next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private TimeOfDay time;

    public TimeOfDayCondition(TimeOfDay time) {
        this.time = time;
    }

    public TimeOfDay getTime() {
        return time;
    }

    public void setTime(TimeOfDay time) {
        this.time = time;
    }

    @Override
    public String type() {
        return "time_of_day";
    }

    @Override
    public String description() {
        return "time of day is " + time.label().toLowerCase();
    }

    @Override
    public boolean test(Minecraft mc) {
        if (mc.level == null) return false;
        long t = mc.level.getOverworldClockTime() % 24000;
        boolean day = t < 13000;
        return time == TimeOfDay.DAY ? day : !day;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("time", time.name());
    }
}
