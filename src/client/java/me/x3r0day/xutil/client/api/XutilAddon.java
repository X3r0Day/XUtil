package me.x3r0day.xutil.client.api;

/**
 * Implement this and list the class under "xutil:addons" in your
 * fabric.mod.json — see ADDON_API.md.
 */
public interface XutilAddon {

    /**
     * Called once during client startup. Register your modules and
     * categories here.
     */
    void onInitializeAddon();
}
