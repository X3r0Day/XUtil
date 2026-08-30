package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BlockInFrontCondition extends MacroCondition {

    private static final double RANGE = 4.5;

    @Override
    public String type() {
        return "block_front";
    }

    @Override
    public String description() {
        return "solid block within " + (int) RANGE + " blocks ahead";
    }

    @Override
    public boolean test(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        HitResult hit = mc.player.pick(RANGE, 1.0f, false);
        return hit instanceof BlockHitResult blockHit
            && !mc.level.getBlockState(blockHit.getBlockPos()).isAir();
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
