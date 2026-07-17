package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;

import com.ericsson.toggledifficultyfeatures.ToggleDifficultyFeatures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Mutable
    @Shadow
    private int numGroups;

    @Shadow
    private int raidOmenLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void enforceHardWaveCountOnEasy(ServerLevel level, CallbackInfo ci) {
        if (this.numGroups == 3
                && level.getGameRules().get(ToggleDifficultyFeatures.HARD_MODE_RAID_WAVES)) {
            this.numGroups = 7 + (this.raidOmenLevel >= 2 ? 1 : 0);
        }
    }
}
