# Toggle Difficulty Features — Implementation Handover

Minecraft 26.2 Fabric mod (Mojang mappings). **Verified against decompiled 26.2 server jar.**

## What it does

1. Forces 100% villager → zombie villager conversion on any difficulty.
2. Mimics Hard-mode raid wave counts on Easy difficulty (7 waves, or 8 with Bad Omen II).
3. Both features have `/gamerule` toggles (default: on).

## Server-side only

The mod works **purely server-side** — no client install needed.
- All mixins target `minecraft-extracted_server.jar` classes (Zombie, Raid, ServerLevel, GameRules).
- Empty `ToggleDifficultyFeaturesClient` entrypoint — Fabric Loader skips it on dedicated servers.
- `fabric.mod.json` has `"environment": "*"` so it loads everywhere, but has zero client-side deps.

## Project config

Already correct — Java 25, Fabric Loader 0.19.3, Fabric API 0.155.0+26.2, Loom 1.17-SNAPSHOT, Mojang mappings.

## Files

### `toggle-difficulty-features.mixins.json`

```json
{
  "required": true,
  "package": "com.ericsson.toggledifficultyfeatures.mixin",
  "compatibilityLevel": "JAVA_25",
  "mixins": [
    "ZombieMixin",
    "RaidMixin",
    "GameRulesInvoker"
  ],
  "injectors": { "defaultRequire": 1 }
}
```

---

### `GameRulesInvoker.java`

`@Invoker` accessor to expose `GameRules.registerBoolean` (private in 26.2).

```java
package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.class)
public interface GameRulesInvoker {
    @Invoker("registerBoolean")
    static GameRule<Boolean> callRegisterBoolean(String name, GameRuleCategory category, boolean defaultValue) {
        throw new AssertionError("Mixin failed to apply");
    }
}
```

---

### `ToggleDifficultyFeatures.java`

Registers two gamerules in `onInitialize()`:

- `/gamerule force_zombie_conversion true|false`
- `/gamerule hard_mode_raid_waves true|false`

```java
public class ToggleDifficultyFeatures implements ModInitializer {
    public static GameRule<Boolean> FORCE_ZOMBIE_CONVERSION;
    public static GameRule<Boolean> HARD_MODE_RAID_WAVES;

    @Override
    public void onInitialize() {
        FORCE_ZOMBIE_CONVERSION = GameRulesInvoker.callRegisterBoolean(
            "force_zombie_conversion", GameRuleCategory.MOBS, true);
        HARD_MODE_RAID_WAVES = GameRulesInvoker.callRegisterBoolean(
            "hard_mode_raid_waves", GameRuleCategory.MOBS, true);
        LOGGER.info("ToggleDifficultyFeatures initialized");
    }
}
```

---

### `ZombieMixin.java`

`@Redirect` on all `ServerLevel.getDifficulty()` calls inside `killedEntity`.

```java
@Mixin(Zombie.class)
public class ZombieMixin {
    @Redirect(
        method = "killedEntity",
        at = @At("INVOKE target='ServerLevel;getDifficulty()'")
    )
    private Difficulty forceHardConversion(ServerLevel level) {
        if (!level.getGameRules().get(ToggleDifficultyFeatures.FORCE_ZOMBIE_CONVERSION)) {
            return level.getDifficulty();
        }
        return Difficulty.HARD;
    }
}
```

**Gamerule check**: Has `ServerLevel` directly (`killedEntity` passes it as a parameter) — trivial.

**Why `@Redirect` not `@ModifyVariable`**: In 26.2, `killedEntity` calls `ServerLevel.getDifficulty()` 3 separate times (compared to NORMAL, then HARD, then HARD again for 100% vs 50% branch). No single local variable to modify. Redirecting ALL getDifficulty calls to return HARD makes every comparison fall through to the "always convert" path.

---

### `RaidMixin.java`

`@Inject` into `tick(ServerLevel)` instead of the constructor (constructor has no Level reference).

```java
@Mixin(Raid.class)
public abstract class RaidMixin {
    @Shadow private int numGroups;
    @Shadow private int raidOmenLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void enforceHardWaveCountOnEasy(ServerLevel level, CallbackInfo ci) {
        if (this.numGroups == 3
                && level.getGameRules().get(ToggleDifficultyFeatures.HARD_MODE_RAID_WAVES)) {
            this.numGroups = 7 + (this.raidOmenLevel >= 2 ? 1 : 0);
        }
    }
}
```

**Gamerule check**: Raid doesn't store a Level reference. But `tick(ServerLevel)` is called every game tick, so checking at `@At("HEAD")` gives us the Level. The `numGroups == 3` guard means it only fires once (on Easy difficulty, default wave count is 3) on the first tick, which is imperceptible.

**`numGroups` not `waveCount`**: Verified via `javap -p` on 26.2 jar — `private int numGroups;` is the real Mojang name.

---

## Gamerule commands

```
/gamerule force_zombie_conversion false    # disable 100% conversion
/gamerule hard_mode_raid_waves false        # restore default Easy wave counts (3 waves)
/gamerule force_zombie_conversion true     # re-enable
/gamerule hard_mode_raid_waves true         # re-enable
```

Changes take effect immediately — no world reload needed.

## Build & run

```
./gradlew build
```
Output `.jar` in `build/libs/`. Works on a pure Fabric dedicated server.

## ponytail notes

- No config file — gamerules replace the need for one.
- No tests — Minecraft mixins are tested in-game.
- RaidMixin overrides at `@At("HEAD")` of `tick()`, so the gamerule is checked once per tick. The `numGroups == 3` guard makes it a single write per raid. If the gamerule is toggled off mid-raid, numGroups stays at the overridden value (correct — waves already spawned can't be un-spawned).
