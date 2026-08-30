package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class HotbarTask extends MacroTask {

    private int slot;

    public HotbarTask(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public String type() {
        return "hotbar";
    }

    @Override
    public String description() {
        return "Select hotbar slot " + slot;
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.player != null && mc.getConnection() != null) {
            int index = slot - 1;
            if (mc.player.getInventory().getSelectedSlot() != index) {
                mc.player.getInventory().setSelectedSlot(index);
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(index));
            }
        }
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("slot", slot);
    }
}
