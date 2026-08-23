package me.x3r0day.xutil.client.addon;

import com.mojang.logging.LogUtils;
import me.x3r0day.xutil.client.api.XutilAddon;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AddonLoader {

    public record Failure(String modId, String name, String error) {
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Failure> FAILURES = new ArrayList<>();
    private static boolean warningShown;

    private AddonLoader() {
    }

    public static void load() {
        try {
            for (EntrypointContainer<XutilAddon> container : FabricLoader.getInstance()
                    .getEntrypointContainers("xutil:addons", XutilAddon.class)) {
                String modId = container.getProvider().getMetadata().getId();
                String name = container.getProvider().getMetadata().getName();
                try {
                    container.getEntrypoint().onInitializeAddon();
                    LOGGER.info("Loaded addon {} ({})", modId, name);
                } catch (Throwable throwable) {
                    String error = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
                    FAILURES.add(new Failure(modId, name, error));
                    LOGGER.error("Failed to load addon {}: {}", modId, error, throwable);
                }
            }
        } catch (Throwable throwable) {
            LOGGER.error("Failed to scan addon entrypoints", throwable);
        }
    }

    public static List<Failure> getFailures() {
        return Collections.unmodifiableList(FAILURES);
    }

    public static boolean shouldShowWarning() {
        return !warningShown && !FAILURES.isEmpty();
    }

    public static void markWarningShown() {
        warningShown = true;
    }
}
