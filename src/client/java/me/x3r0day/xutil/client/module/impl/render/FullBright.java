package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;

public class FullBright extends Module {

    private static volatile boolean active;

    public FullBright() {
        super("FullBright", "Makes the world fully lit.", Category.RENDER);
    }

    public static boolean isActive() {
        return active;
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
