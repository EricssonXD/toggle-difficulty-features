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
