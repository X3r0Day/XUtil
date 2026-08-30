package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.slf4j.Logger;

public final class UseTask extends MacroTask {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_HOLD_TICKS = 100;

    private boolean started;
    private int heldTicks;

    @Override
    public String type() {
        return "use";
    }

    @Override
    public String description() {
        return "Use the held item, holds until finished";
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null || mc.options == null) return true;

        if (!started) {
            started = true;
            heldTicks = 0;
            // the vanilla input handler releases any use when the key is not
            // held, so keep the key forced down while the item is in use
            mc.options.keyUse.setDown(true);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            LOGGER.info("use: clicked {}", mc.player.getMainHandItem().getItem());
            return false;
        }

        if (!mc.player.isUsingItem()) {
            mc.options.keyUse.setDown(false);
            LOGGER.info("use: finished after {} ticks", heldTicks);
            started = false;
            return true;
        }

        if (++heldTicks >= MAX_HOLD_TICKS) {
            mc.options.keyUse.setDown(false);
            mc.gameMode.releaseUsingItem(mc.player);
            LOGGER.info("use: released after {} ticks (timeout)", heldTicks);
            started = false;
            return true;
        }
        return false;
    }

    public static void releaseHeldKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.keyUse.setDown(false);
        }
    }

    @Override
    public void reset() {
        if (started) {
            releaseHeldKey();
        }
        started = false;
        heldTicks = 0;
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
