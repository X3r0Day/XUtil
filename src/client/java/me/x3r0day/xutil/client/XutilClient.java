package me.x3r0day.xutil.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.x3r0day.xutil.client.addon.AddonLoader;
import me.x3r0day.xutil.client.module.ModuleManager;
import me.x3r0day.xutil.client.module.impl.world.WorldInfo;
import me.x3r0day.xutil.client.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class XutilClient implements ClientModInitializer {

    public static final String MOD_ID = "xutil";

    private static KeyMapping openClickGui;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openClickGui = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.xutil.open_click_gui", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DELETE, category));

        ModuleManager.init();
        AddonLoader.load();

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(MOD_ID, "worldinfo"),
            (graphics, deltaTracker) -> WorldInfo.render(graphics, Minecraft.getInstance())
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);

            while (openClickGui.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) {
                    client.gui.setScreen(new ClickGuiScreen());
                }
            }
        });
    }
}
