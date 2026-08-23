package me.x3r0day.xutil.client.module;

import com.mojang.logging.LogUtils;
import me.x3r0day.xutil.client.module.impl.world.WorldInfo;
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
        register(new WorldInfo());
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
            if (module.isEnabled()) {
                module.onTick(mc);
            }
        }
    }
}
