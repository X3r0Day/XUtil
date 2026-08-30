package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class EntityHurtCondition extends MacroCondition {

    @Override
    public String type() {
        return "entity_hurt";
    }

    @Override
    public String description() {
        return "entity under crosshair was recently hurt";
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.hitResult instanceof EntityHitResult hit
            && hit.getEntity() instanceof LivingEntity living && living.hurtTime > 0;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
