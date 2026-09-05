package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class LookTask extends MacroTask {

    private static final float MAX_SMOOTH_YAW = 50f;
    private static final float MAX_SMOOTH_PITCH = 25f;
    private static final float SMOOTH_ACCEL = 16f;

    public enum Mode {
        TURN("Turn"),
        AIM("Aim");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Sort {
        NEAREST("Nearest"),
        LOWEST_HEALTH("Lowest health"),
        HIGHEST_HEALTH("Highest health");

        private final String label;

        Sort(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Sort next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Filter {
        ANY("Any"),
        PLAYERS("Players"),
        MOBS("Mobs");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private Mode mode = Mode.TURN;
    private double yaw;
    private double pitch;
    private double range = 8.0;
    private Sort sort = Sort.NEAREST;
    private Filter filter = Filter.ANY;
    private boolean smooth = true;

    private float velYaw;
    private float velPitch;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    @Override
    public String type() {
        return "look";
    }

    @Override
    public String description() {
        if (mode == Mode.TURN) {
            return "Turn yaw " + format(yaw) + ", pitch " + format(pitch);
        }
        return "Aim at " + sort.label().toLowerCase() + " " + filter.label().toLowerCase()
            + " (" + (int) range + "m" + (smooth ? ", smooth" : "") + ")";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player == null) return true;

        if (mode == Mode.TURN) {
            mc.player.turn(yaw, pitch);
            return true;
        }

        LivingEntity best = pickTarget(mc);
        if (best == null) {
            velYaw *= 0.6f;
            velPitch *= 0.6f;
            return true;
        }

        aimAt(mc, best);
        return true;
    }

    private LivingEntity pickTarget(Minecraft mc) {
        LivingEntity best = null;
        double bestScore = sort == Sort.HIGHEST_HEALTH ? Double.MIN_VALUE : Double.MAX_VALUE;
        double rangeSqr = range * range;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !matches(living, mc.player)) continue;
            double distSqr = living.distanceToSqr(mc.player);
            if (distSqr > rangeSqr) continue;

            double score = switch (sort) {
                case NEAREST -> distSqr;
                case LOWEST_HEALTH -> living.getHealth();
                case HIGHEST_HEALTH -> living.getHealth();
            };
            boolean better = sort == Sort.HIGHEST_HEALTH ? score > bestScore : score < bestScore;
            if (better) {
                bestScore = score;
                best = living;
            }
        }
        return best;
    }

    private boolean matches(LivingEntity target, Player self) {
        if (target == self || !target.isAlive()) return false;
        return switch (filter) {
            case PLAYERS -> target instanceof Player;
            case MOBS -> !(target instanceof Player);
            case ANY -> true;
        };
    }

    private void aimAt(Minecraft mc, LivingEntity target) {
        Vec3 eye = mc.player.getEyePosition(1.0f);
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();
        double dy = target.getEyeY() - eye.y;

        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitchTo = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        if (smooth) {
            float curYaw = mc.player.getYRot();
            float curPitch = mc.player.getXRot();
            float deltaYaw = wrap180(yawTo - curYaw);
            float deltaPitch = pitchTo - curPitch;

            float targetVelYaw = clamp(deltaYaw * 0.7f, -MAX_SMOOTH_YAW, MAX_SMOOTH_YAW);
            float targetVelPitch = clamp(deltaPitch * 0.7f, -MAX_SMOOTH_PITCH, MAX_SMOOTH_PITCH);
            velYaw = approach(velYaw, targetVelYaw, SMOOTH_ACCEL);
            velPitch = approach(velPitch, targetVelPitch, SMOOTH_ACCEL);

            if (Math.abs(deltaYaw) < 1.0f && Math.abs(velYaw) < 1.2f) {
                float jitter = (float) (Math.random() * 0.5f - 0.25f);
                mc.player.setYRot(curYaw + jitter);
            } else {
                mc.player.setYRot(curYaw + velYaw);
            }
            if (Math.abs(deltaPitch) < 1.0f && Math.abs(velPitch) < 1.2f) {
                float jitter = (float) (Math.random() * 0.4f - 0.2f);
                mc.player.setXRot(Math.max(-90f, Math.min(90f, curPitch + jitter)));
            } else {
                mc.player.setXRot(Math.max(-90f, Math.min(90f, curPitch + velPitch)));
            }
        } else {
            mc.player.setYRot(yawTo);
            mc.player.setXRot(pitchTo);
        }
    }

    private static float wrap180(float degrees) {
        float d = (degrees + 540f) % 360f;
        return d - 180f;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float approach(float v, float target, float step) {
        if (v < target) return Math.min(target, v + step);
        return Math.max(target, v - step);
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("mode", mode.name());
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
        json.addProperty("range", range);
        json.addProperty("sort", sort.name());
        json.addProperty("filter", filter.name());
        json.addProperty("smooth", smooth);
    }

    public static LookTask fromJson(JsonObject json) {
        LookTask task = new LookTask();
        task.mode = safeMode(GsonHelper.getAsString(json, "mode", "TURN"));
        task.yaw = GsonHelper.getAsDouble(json, "yaw", 0);
        task.pitch = GsonHelper.getAsDouble(json, "pitch", 0);
        task.range = GsonHelper.getAsDouble(json, "range", 8.0);
        task.sort = safeSort(GsonHelper.getAsString(json, "sort", "NEAREST"));
        task.filter = safeFilter(GsonHelper.getAsString(json, "filter", "ANY"));
        task.smooth = GsonHelper.getAsBoolean(json, "smooth", true);
        return task;
    }

    private static Mode safeMode(String value) {
        try {
            return Mode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mode.TURN;
        }
    }

    private static Sort safeSort(String value) {
        try {
            return Sort.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Sort.NEAREST;
        }
    }

    private static Filter safeFilter(String value) {
        try {
            return Filter.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Filter.ANY;
        }
    }

    private static String format(double value) {
        return String.valueOf((int) value);
    }
}
