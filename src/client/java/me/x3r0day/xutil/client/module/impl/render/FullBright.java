package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class FullBright extends Module {

    private static volatile boolean active;

    public FullBright() {
        super("FullBright", "Makes the world fully lit.", Category.RENDER);
    }

    public static boolean isActive() {
        return active;
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(OptionListScreen.createKeybindScreen("FullBright",
                "Bind a key to toggle the module", getKeybind(), parent));
        }
    }

    @Override
    protected void onEnable() {
        active = true;
    }

    @Override
    protected void onDisable() {
        active = false;
    }
}
