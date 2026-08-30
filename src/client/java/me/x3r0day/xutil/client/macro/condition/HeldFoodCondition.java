package me.x3r0day.xutil.client.macro.condition;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;

public final class HeldFoodCondition extends MacroCondition {

    @Override
    public String type() {
        return "held_food";
    }

    @Override
    public String description() {
        return "the held item is food";
    }

    @Override
    public boolean test(Minecraft mc) {
        return mc.player != null && mc.player.getMainHandItem().has(DataComponents.FOOD);
    }

    @Override
    public void toJson(JsonObject json) {
    }
}
