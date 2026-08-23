package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EntityInHitboxCondition extends MacroCondition {

    @Override
    public String type() {
        return "entity_hitbox";
    }

    @Override
    public String description() {
        return "living entity under the crosshair in reach";
    }

    @Override
    public boolean test(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;

        Vec3 eye = mc.player.getEyePosition(1.0f);
        Vec3 end = eye.add(mc.player.getViewVector(1.0f).scale(mc.player.entityInteractionRange()));

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == mc.player || !living.isAlive()) {
                continue;
            }
            if (living.getBoundingBox().clip(eye, end).isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
