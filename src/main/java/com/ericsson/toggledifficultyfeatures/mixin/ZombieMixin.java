package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.server.level.ServerLevel;

import com.ericsson.toggledifficultyfeatures.ToggleDifficultyFeatures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Zombie.class)
public class ZombieMixin {
    @Redirect(
        method = "killedEntity",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;")
    )
    private Difficulty forceHardConversion(ServerLevel level) {
        if (!level.getGameRules().get(ToggleDifficultyFeatures.FORCE_ZOMBIE_CONVERSION)) {
            return level.getDifficulty();
        }
        return Difficulty.HARD;
    }
}
