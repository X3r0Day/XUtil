package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;

public final class AttackTask extends MacroTask {

    @Override
    public String type() {
        return "attack";
    }

    @Override
    public String description() {
        return "Attack the entity under the crosshair";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player != null && mc.gameMode != null
            && mc.hitResult instanceof EntityHitResult hit) {
            mc.gameMode.attack(mc.player, hit.getEntity());
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
