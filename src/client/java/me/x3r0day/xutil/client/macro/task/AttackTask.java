package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class AttackTask extends MacroTask {

    public enum Mode {
        CROSSHAIR("Crosshair"),
        NEAREST("Nearest"),
        ALL("All");

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

    private Mode mode = Mode.CROSSHAIR;
    private Filter filter = Filter.ANY;
    private double range = 4.5;
    private boolean waitCooldown;
    private long lastSwingTick = -100;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public boolean isWaitCooldown() {
        return waitCooldown;
    }

    public void setWaitCooldown(boolean waitCooldown) {
        this.waitCooldown = waitCooldown;
    }

    @Override
    public String type() {
        return "attack";
    }

    @Override
    public String description() {
        String base = mode == Mode.CROSSHAIR
            ? "Attack the entity under the crosshair"
            : "Attack " + mode.label().toLowerCase() + " (" + filter.label() + ", " + (int) range + "m)";
        return base + (waitCooldown ? ", wait for cooldown" : "");
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null || mc.level == null) return true;

        if (waitCooldown) {
            int delay = Math.max(1, (int) Math.ceil(mc.player.getCurrentItemAttackStrengthDelay()));
            if (mc.level.getGameTime() - lastSwingTick < delay) {
                return false;
            }
        }

        switch (mode) {
            case CROSSHAIR -> {
                if (mc.hitResult instanceof EntityHitResult hit) {
                    attack(mc, hit.getEntity());
                }
            }
            case NEAREST -> {
                LivingEntity best = null;
                double bestSqr = Double.MAX_VALUE;
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (!(entity instanceof LivingEntity living) || !matches(living, mc.player)) continue;
                    double sqr = living.distanceToSqr(mc.player);
                    if (sqr > range * range || sqr >= bestSqr) continue;
                    bestSqr = sqr;
                    best = living;
                }
                if (best != null) {
                    attack(mc, best);
                }
            }
            case ALL -> {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof LivingEntity living && matches(living, mc.player)
                        && living.distanceToSqr(mc.player) <= range * range) {
                        attack(mc, living);
                    }
                }
            }
        }
        return true;
    }

    private boolean matches(LivingEntity target, Player self) {
        if (target == self || !target.isAlive()) return false;
        return switch (filter) {
            case PLAYERS -> target instanceof Player;
            case MOBS -> !(target instanceof Player);
            case ANY -> true;
        };
    }

    private void attack(Minecraft mc, Entity target) {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        lastSwingTick = mc.level.getGameTime();
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("mode", mode.name());
        json.addProperty("filter", filter.name());
        json.addProperty("range", range);
        json.addProperty("wait_cooldown", waitCooldown);
    }

    public static AttackTask fromJson(JsonObject json) {
        AttackTask task = new AttackTask();
        task.mode = safeMode(GsonHelper.getAsString(json, "mode", "CROSSHAIR"));
        task.filter = safeFilter(GsonHelper.getAsString(json, "filter", "ANY"));
        task.range = GsonHelper.getAsDouble(json, "range", 4.5);
        task.waitCooldown = GsonHelper.getAsBoolean(json, "wait_cooldown", false);
        return task;
    }

    private static Mode safeMode(String value) {
        try {
            return Mode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mode.CROSSHAIR;
        }
    }

    private static Filter safeFilter(String value) {
        try {
            return Filter.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Filter.ANY;
        }
    }
}
