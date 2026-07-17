package com.ericsson.toggledifficultyfeatures;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import com.ericsson.toggledifficultyfeatures.mixin.GameRulesInvoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleDifficultyFeatures implements ModInitializer {
	public static final String MOD_ID = "toggle-difficulty-features";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
