package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EntityInRangeCondition extends MacroCondition {

    private double radius;

    public EntityInRangeCondition(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public String type() {
        return "entity_range";
    }

    @Override
    public String description() {
        return "living entity within " + (int) radius + " blocks";
    }

    @Override
    public boolean test(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity && entity != mc.player && entity.isAlive()
                && entity.distanceToSqr(mc.player) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("radius", radius);
    }
}
