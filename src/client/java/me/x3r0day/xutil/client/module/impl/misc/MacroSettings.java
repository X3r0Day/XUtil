package me.x3r0day.xutil.client.module.impl.misc;

import me.x3r0day.xutil.client.macro.MacroManager;
import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.MacroListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MacroSettings extends Module {

    public MacroSettings() {
        super("Macro Settings",
            "Right-click to manage your macros. Toggle off to stop running macros.",
            Category.MISC);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new MacroListScreen(parent));
        }
    }

    @Override
    protected void onDisable() {
        MacroManager.stopAll();
    }
}
