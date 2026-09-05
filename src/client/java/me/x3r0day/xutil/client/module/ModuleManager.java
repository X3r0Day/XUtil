package me.x3r0day.xutil.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import me.x3r0day.xutil.client.module.impl.misc.MacroSettings;
import me.x3r0day.xutil.client.module.impl.misc.Repeater;
import me.x3r0day.xutil.client.module.impl.movement.Sprint;
import me.x3r0day.xutil.client.module.impl.render.BreakIndicator;
import me.x3r0day.xutil.client.module.impl.render.FullBright;
import me.x3r0day.xutil.client.module.impl.render.TargetHighlight;
import me.x3r0day.xutil.client.module.impl.render.Zoom;
import me.x3r0day.xutil.client.module.impl.world.AutoTool;
import me.x3r0day.xutil.client.module.impl.world.WorldInfo;
import me.x3r0day.xutil.mixin.client.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ModuleManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<Module> MODULES = new CopyOnWriteArrayList<>();
    private static final List<Category> CATEGORIES = new CopyOnWriteArrayList<>();

    private ModuleManager() {
    }

    public static void init() {
        addCategory(Category.WORLD);
        addCategory(Category.RENDER);
        addCategory(Category.MOVEMENT);
        addCategory(Category.MISC);
        register(new WorldInfo());
        register(new AutoTool());
        register(new FullBright());
        register(new BreakIndicator());
        register(new TargetHighlight());
        register(new Zoom());
        register(new Sprint());
        register(new Repeater());
        register(new MacroSettings());
    }

    public static void register(Module module) {
        for (Module existing : MODULES) {
            if (existing.getName().equals(module.getName())) {
                LOGGER.warn("Skipping duplicate module registration: {}", module.getName());
                return;
            }
        }
        MODULES.add(module);
    }

    public static void unregister(Module module) {
        MODULES.remove(module);
    }

    public static void addCategory(Category category) {
        for (Category existing : CATEGORIES) {
            if (existing.equals(category)) {
                LOGGER.warn("Skipping duplicate category registration: {}", category.getDisplayName());
                return;
            }
        }
        CATEGORIES.add(category);
    }

    public static List<Category> getCategories() {
        return Collections.unmodifiableList(CATEGORIES);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) {
            if (module.getCategory().equals(category)) {
                result.add(module);
            }
        }
        return result;
    }

    public static void tick(Minecraft mc) {
        for (Module module : MODULES) {
            if (module.usesDefaultKeybindToggle()) {
                KeyMapping keybind = module.getKeybind();
                if (keybind != null && !keybind.isUnbound()) {
                    int key = ((KeyMappingAccessor) keybind).xutil$getKey().getValue();
                    boolean down = InputConstants.isKeyDown(mc.getWindow(), key);
                    if (down && !module.isKeyWasDown()) {
                        module.toggle();
                    }
                    module.setKeyWasDown(down);
                }
            }
            if (module.isEnabled()) {
                module.onTick(mc);
            }
        }
    }
}
