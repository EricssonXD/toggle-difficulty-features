# Toggle Difficulty Features — Implementation Handover

Minecraft 26.2 Fabric mod (Mojang mappings).

## What it does

1. Forces 100% villager → zombie villager conversion on any difficulty.
2. Mimics Hard-mode raid wave counts on Easy difficulty (7 waves, or 8 with Bad Omen II).

## Project config (already correct)

All build files in the repo are properly configured for 26.2:
- Java 25, Fabric Loader 0.19.3, Fabric API 0.155.0+26.2, Loom 1.17-SNAPSHOT
- Mojang mappings (the default for 26.1+ — no Yarn dependency needed)

## What to add

### 1. Mixin config — create `src/main/resources/toggle-difficulty-features.mixins.json`

The project already references this file in `fabric.mod.json`. Create it:

```json
{
  "required": true,
  "package": "com.ericsson.toggledifficultyfeatures.mixin",
  "compatibilityLevel": "JAVA_25",
  "mixins": [
    "ZombieMixin",
    "RaidMixin"
  ],
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### 2. ZombieMixin — `src/main/java/com/ericsson/toggledifficultyfeatures/mixin/ZombieMixin.java`

```java
package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Zombie.class)
public class ZombieMixin {
    @ModifyVariable(
        method = "onKilledOther",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getDifficulty()Lnet/minecraft/world/Difficulty;"
        )
    )
    private Difficulty forceHardConversion(Difficulty original) {
        return Difficulty.HARD;
    }
}
```

### 3. RaidMixin — `src/main/java/com/ericsson/toggledifficultyfeatures/mixin/RaidMixin.java`

```java
package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Shadow
    private int waveCount;

    @Shadow
    public abstract Level level();

    @Shadow
    private int raidOmenLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void overrideWaveCountOnEasy(CallbackInfo ci) {
        if (this.level().getDifficulty() == Difficulty.EASY) {
            this.waveCount = 7 + (this.raidOmenLevel >= 2 ? 1 : 0);
        }
    }
}
```

## Key changes from initial handover

| What | Wrong | Right |
|------|-------|-------|
| Version | 1.21.11 | **26.2** |
| Mappings | Yarn (`yarn:1.21.11+build.1:v2`) | **Mojang** (default since 26.1) |
| Java | 21 | **25** |
| Plugin | `id 'fabric-loom'` | `id 'net.fabricmc.fabric-loom'` (already in build.gradle) |
| Zombie class | `ZombieEntity` | `Zombie` (Mojang name) |
| Raid package | `net.minecraft.village.raid` | `net.minecraft.world.entity.raid` |
| ServerWorld | `ServerWorld` | `ServerLevel` (Mojang name) |
| Raid guard | No difficulty check | Only overrides on `Difficulty.EASY` |
| `@ModifyVariable` index | `index = 8` | Removed — using `INVOKE` target instead, which is more stable across recompiles |
| Build/deps files | Included in handover | **Omitted** — project already has correct ones |

## Verification

- **Zombie conversion**: Easy difficulty, let zombie kill a villager → converts 100%.
- **Raid waves**: Easy difficulty, trigger a raid → 7 waves (Bad Omen I), 8 waves (Bad Omen II).
- **Normal/Hard untouched**: Raid waves on Normal (5) and Hard (7) should be unchanged. Zombie conversion still forced (the `ZombieMixin` has no difficulty guard — intentional, per spec "any difficulty").

## ponytail notes

- Mojang mapping class/field names were cross-referenced via Paper API 26.2, Fabric docs, and community sources. If `level()` or `raidOmenLevel` don't resolve at compile, run `/ponytail debug` to grep the actual 26.2 decompiled Raid source.
- No config file — hardcoded. Add `cloth-config` or `fabric-command-api` toggle only if someone asks.
- No unit tests — Minecraft mixins are tested in-game. Don't add a test framework.
