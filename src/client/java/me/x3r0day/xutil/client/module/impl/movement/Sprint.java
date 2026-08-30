package me.x3r0day.xutil.client.module.impl.movement;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import me.x3r0day.xutil.client.ui.OptionListScreen.KeybindRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Always sprint while moving.", Category.MOVEMENT);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new OptionListScreen("Sprint", "Bind a key to toggle the module",
                List.of(), List.of(new KeybindRow("Keybind", getKeybind())), parent));
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.options == null) return;

        if (mc.gui.screen() != null) {
            mc.options.keySprint.setDown(false);
            return;
        }

        boolean moving = mc.player.input.getMoveVector().y > 0;
        if (moving && !mc.options.keyShift.isDown() && !mc.player.isUsingItem()) {
            mc.options.keySprint.setDown(true);
        } else {
            mc.options.keySprint.setDown(false);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.keySprint.setDown(false);
        }
    }
}
