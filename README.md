# Toggle Difficulty Features

A Fabric mod for Minecraft 26.2.

## Features

- Forces villager → zombie villager conversion to 100% on every difficulty.
- Gives Easy raids Hard-mode wave counts: 7 waves, or 8 with Bad Omen II+.
- Both features are enabled by default and can be toggled with gamerules.

## Gamerules

```text
/gamerule force_zombie_conversion false
/gamerule hard_mode_raid_waves false
```

Set either rule to `true` to re-enable it. Changes apply immediately.

## Installation

This mod is server-side only; clients do not need to install it.

1. Install a Fabric 26.2 dedicated server.
2. Install Fabric API.
3. Put the mod jar in the server's `mods` directory.

Requirements: Java 25 and Fabric Loader 0.19.3 or newer.

## Development

```sh
./gradlew build
./gradlew runServer
```

The built jar is written to `build/libs/`.

## License

CC0-1.0.
