# Toggle Difficulty Features

A Fabric mod for Minecraft 26.2.

## Features

- Forces villager → zombie villager conversion to 100% on every difficulty.
- Gives Easy raids Hard-mode wave counts: 7 waves, or 8 with Bad Omen II+.
- Both difficulty features are enabled by default and can be toggled with gamerules.
- Villager curing discounts can be shared globally with a gamerule (disabled by default).

## Gamerules

```text
/gamerule force_zombie_conversion false
/gamerule hard_mode_raid_waves false
/gamerule global_villager_discounts true
```

Set a rule to `true` to enable it. Changes apply immediately.

## Installation

This mod is server-side only; clients do not need to install it.

1. Install a Fabric 26.2 dedicated server.
2. Install Fabric API.
3. Put the mod jar in the server's `mods` directory.

Requirements: Java 25 and Fabric Loader 0.19.3 or newer.

## Development

```sh
./gradlew runGameTest  # run server GameTests during development
./gradlew assemble      # build the jar without running tests
./gradlew build         # full build, including server GameTests
./gradlew runServer     # launch a development server
```

The built jar is written to `build/libs/`. GameTests use a real Minecraft server, so startup takes longer than unit tests. Run `runGameTest` while iterating and reserve `build` for full local or CI verification.

See the [Fabric automated testing guide](https://docs.fabricmc.net/develop/automatic-testing) for details.

## License

CC0-1.0.
