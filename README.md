<div align="center">

# ⚡ XUtil

### A Minecraft Fabric utility mod for quality of life

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-8A5CFF?style=for-the-badge&logo=mojangstudios&logoColor=white)](https://www.minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-API-8A5CFF?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBmaWxsPSIjZmZmIiBkPSJNOCAwaDh2OHpNMCAwaDh2OHpNMCA4aDh2OHpNOCA4aDh2OHoiLz48L3N2Zz4=)](https://fabricmc.net)
[![Version](https://img.shields.io/badge/version-1.2.1-8A5CFF?style=for-the-badge)](https://github.com/X3r0Day/XUtil/releases)
[![License](https://img.shields.io/badge/license-MIT-8A5CFF?style=for-the-badge)](LICENSE.txt)

<br>

A utility mod that adds the small things Minecraft is missing.
No cheats, no bloat. Just quality of life.

</div>

---

## ✨ What's inside

| Module | Category | What it does |
|---|---|---|
| **ClickGUI** | — | `DEL` to open, draggable windows, live search, custom accent color |
| **WorldInfo** | World | Biome, coords, time, day, facing, light, FPS, entities on the HUD |
| **AutoTool** | World | Picks the best tool for the block you break, enchants included |
| **FullBright** | Render | Full brightness, everywhere |
| **BreakIndicator** | Render | Watch the block you're mining fill up, rainbow mode included |
| **TargetHighlight** | Render | Boxes the entity under your crosshair |
| **Zoom** | Render | A bindable spyglass that doesn't cost an inventory slot |
| **Sprint** | Movement | Hold forward and go |
| **Recorder** | Misc | Record your inputs and replay them |
| **Macro Settings** | Misc | The hub for everything macro related |

### 🎬 Recorder

Record movement, look, hotbar switches, held keys, **and module toggles**. Two dedicated keys (record + play), loop mode, per-recording mouse lock, and it resets you back to the start position every replay.

> [!NOTE]
> Recordings are tiny RLE keyframes, saved as pretty JSON. Drop any recording into a macro with the `Replay` task.

### 🔁 Macros

Task chains: `Chat`, `Attack`, `Use`, `Wait`, `WaitUntil`, `Walk`, `Jump`, `Turn/Aim`, `Hotbar`, `Toggle Module`, `Replay`, `If`, `Loop`, `Break`.

Statements: `If` and `WaitUntil` take any number of conditions combined with **match all** / **match any** and per-condition **NOT**.

| Group | Conditions |
|---|---|
| **Player** | Health below, health above, hunger below, held is food, on ground, in water |
| **World** | Time of day, block in front |
| **Entity** | In range, in hitbox, recently hurt |
| **Other** | Always |

Triggers: keybind, key toggle, once on enable, or every tick. Config hot-reloads while the game runs.

> [!TIP]
> Every module has a bindable key. Right-click it in the ClickGUI, click the keybind row, press a key.

### 👤 Account Switching

Swap between offline and Microsoft accounts without restarting. Sessions persist.

---

## 📦 Installation

1. Install [Fabric](https://fabricmc.net/use) for Minecraft 26.2
2. Download the jar from [releases](https://github.com/X3r0Day/XUtil/releases)
3. Drop it into your `mods` folder
4. Press `DEL` in-game

### Building from source

```bash
git clone https://github.com/X3r0Day/XUtil.git
cd XUtil
./gradlew build
```

The jar lands in `build/libs/`. Requires Java 25.

> [!WARNING]
> XUtil is a client-side mod. It works in singleplayer and on servers but few servers may trigger Automation AC so use at your own risk!

---

## 🗂️ Config

All files live in the Minecraft `config/` folder, pretty-printed and hand-editable:

| File | Contents |
|---|---|
| `xutil-modules.json` | Module states and sub-settings |
| `xutil-macros.json` | Macros and the chain speed cap |
| `xutil-recordings.json` | Recorded inputs |
| `xutil-keybinds.json` | Module keybinds |
| `xutil-theme.json` | GUI accent color |
| `xutil-windows.json` | ClickGUI window positions |

---

## 🧩 Addons

XUtil supports addon jars dropped into `mods/`, like a lightweight plugin system. Addons register their own modules and ClickGUI categories through the `xutil:addons` entrypoint.

See [ADDON_API.md](ADDON_API.md).

---

## 📜 License

[MIT](LICENSE.txt)
