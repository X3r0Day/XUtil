# XUtil

A small client utility fabric mod for Minecraft 26.2. ClickGUI, world info HUD, and account switching.

## Features

- **ClickGUI** - press `DEL` in-game to open the module menu. Windows are draggable, the world stays visible behind it. A search box filters modules by name, and a color button cycles the GUI accent color (saved to `config/xutil-theme.json`).
- **WorldInfo** - an on-screen HUD showing biome, coordinates, dimension, game time, day, facing, light level, FPS, and nearby entity count. Right-click the module in the ClickGUI to pick which lines are shown.
- **Macros** - task chains with chat, attack, use, wait, wait-until, walk, jump, turn, module-toggle, if/else, loop (including infinite), and break tasks. `If` and `Wait until` take full statements: any number of conditions combined with match-all / match-any, and per-condition NOT, edited in a dedicated conditions screen. Conditions are grouped (Player, World, Entity, Other): always, entity in range, entity in hitbox, recently hurt, health above/below, hunger below, on ground, in water, time of day, block in front. Triggers: keybind, key toggle, once on enable, or every tick. Chains are edited in game from the ClickGUI or the title screen. The `Macro Settings` module in the Misc category opens the macro list on right-click and stops running chains when toggled off. Macros live in `config/xutil-macros.json` (pretty-printed JSON, hand-editable; old single-condition files migrate automatically).
- **Account Switching** - "Offline Accounts" button on the title screen switches your username without closing the game; the new profile applies on the next connection. Microsoft login is supported via the same flow Meteor Client uses, with the session persisted across restarts.

## Usage

| Action | Input |
|---|---|
| Open / close menu | `DEL` |
| Toggle module | left-click module entry |
| Module options | right-click module entry |

## Build

```
./gradlew build
```

Output jar lands in `build/libs/`. Requires Java 25. Drops into `.minecraft/mods/` like any Fabric mod.

## Project layout

```
src/main/         mod entrypoint + resources
src/client/       client entrypoint, module system, ClickGUI, HUD, account switching, mixins
```

## Addons

XUtil supports addon jars dropped into `mods/` (like Meteor Client addons). Addons register
their own modules and ClickGUI categories via the `xutil:addons` entrypoint. Addon devs can
pull XUtil from JitPack as a dependency. See [ADDON_API.md](ADDON_API.md).

## License

MIT
