# XUtil Addon API

XUtil supports addon jars: separate Fabric mods dropped into `mods/` next to XUtil that
register their own modules and ClickGUI categories at startup.

## Discovery

Addons declare the entrypoint key `"xutil:addons"` in their own `fabric.mod.json`. XUtil
loads every such entrypoint at client startup and calls `onInitializeAddon()` on it.

```json
{
  "schemaVersion": 1,
  "id": "xutil-example-addon",
  "version": "1.0",
  "environment": "client",
  "entrypoints": {
    "xutil:addons": [
      "com.example.addon.ExampleAddon"
    ]
  },
  "depends": {
    "fabricloader": ">=0.19.3",
    "minecraft": "26.2",
    "xutil": ">=1.0"
  }
}
```

## Stable API

Everything in the list below is stable and versioned by `XutilApi.API_VERSION` (currently 1):

- `me.x3r0day.xutil.client.api.XutilAddon` — `void onInitializeAddon()`
- `me.x3r0day.xutil.client.api.XutilApi` — `public static final int API_VERSION = 1`
- `me.x3r0day.xutil.client.module.Category` — `Category(String displayName)`,
  `String getDisplayName()`, builtin instances `Category.WORLD` and `Category.RENDER`
- `me.x3r0day.xutil.client.module.Module` — `Module(String name, String description, Category category)`,
  `getName()`, `getDescription()`, `getCategory()`, `isEnabled()`, `toggle()`,
  `setEnabled(boolean)`, `onSecondaryClick()`, `onTick(Minecraft mc)`,
  protected `onEnable()` / `onDisable()`
- `me.x3r0day.xutil.client.module.ModuleManager` — `addCategory(Category)`,
  `register(Module)`, `getCategories()`, `getModules()`, `getByCategory(Category)`,
  `tick(Minecraft)`

Anything else in the XUtil jar is internal and may change without notice.

## Module lifecycle

- The constructor receives `(name, description, category)`.
- `onEnable()` / `onDisable()` (protected) run when the module is toggled.
- `onTick(Minecraft mc)` runs every client tick while the module is enabled.
- `onSecondaryClick()` runs when the module is right-clicked in the ClickGUI; use it to
  open your own `Screen` with the current screen as parent (see how `WorldInfo` does it).
- `setEnabled(boolean)` / `toggle()` are managed by the ClickGUI; overriding them is not supported.

## Categories

- Use the builtin `Category.WORLD` or `Category.RENDER`, or create your own with
  `new Category("Name")`.
- Custom categories must be registered with `ModuleManager.addCategory(...)` inside
  `onInitializeAddon()` before registering modules that use them.
- Duplicate category names or duplicate module names are ignored with a log warning.

## Broken addons

If an addon's entrypoint throws during startup, XUtil logs the failure and shows a warning
screen on the title screen with an "Open Logs Folder" button. Other addons and the game keep
working.

## Walkthrough

1. Build XUtil: `./gradlew build` (from the repo root).
2. Create your own Fabric mod project (any loom template works) and depend on the built jar:

   ```gradle
   // MC 26.2 doesn't remap, so plain implementation is fine
   implementation files("path/to/XUtil-1.0.jar")
   ```

3. Implement `XutilAddon`, register your modules and categories in `onInitializeAddon()`,
   declare the `xutil:addons` entrypoint in your `fabric.mod.json` (see Discovery above),
   then build and drop your jar into `mods/` next to the XUtil jar.

## Notes

- Addons may use Fabric API themselves (HUD rendering, tick events, etc.).
- A published maven artifact for XUtil is future work; for now depend on the local jar.
