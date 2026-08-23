package me.x3r0day.xutil.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import me.x3r0day.xutil.client.XutilClient;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private final KeyMapping keybind;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this(name, description, category, InputConstants.UNKNOWN.getValue());
    }

    protected Module(String name, String description, Category category, int defaultKey) {
        this(name, description, category, defaultKey, true);
    }

    protected Module(String name, String description, Category category, int defaultKey,
            boolean registerKeybind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = registerKeybind
            ? KeyMappingHelper.registerKeyMapping(new KeyMapping(
                name, InputConstants.Type.KEYSYM, defaultKey, XutilClient.CATEGORY))
            : null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public boolean usesDefaultKeybindToggle() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void onSecondaryClick() {
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendOverlayMessage(Component.literal(
                name + (enabled ? " \u00a7aON" : " \u00a7cOFF")));
        }

        ModuleConfig.save();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(Minecraft mc) {
    }
}
