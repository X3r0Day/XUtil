# XUtil

A small client utility fabric mod for Minecraft 26.2. ClickGUI, world info HUD, and account switching.

## Features

- **ClickGUI** - press `DEL` in-game to open the module menu. Windows are draggable, the world stays visible behind it.
- **WorldInfo** - an on-screen HUD showing biome, coordinates, dimension, game time, day, facing, light level, FPS, and nearby entity count. Right-click the module in the ClickGUI to pick which lines are shown.
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
their own modules and ClickGUI categories via the `xutil:addons` entrypoint. See
[ADDON_API.md](ADDON_API.md).

## License

MIT
