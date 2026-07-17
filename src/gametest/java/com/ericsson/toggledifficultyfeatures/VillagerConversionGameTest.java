package com.ericsson.toggledifficultyfeatures;

import java.lang.reflect.Method;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;

public class VillagerConversionGameTest implements CustomTestMethodInvoker {
	@GameTest
	public void zombieConvertsVillager(GameTestHelper test) {
		ServerLevel level = test.getLevel();
		var server = level.getServer();
		server.setDifficulty(Difficulty.EASY, true);
		level.getGameRules().set(ToggleDifficultyFeatures.FORCE_ZOMBIE_CONVERSION, true, server);

		test.setBlock(1, 0, 1, Blocks.STONE);
		test.setBlock(2, 0, 1, Blocks.STONE);

		var villager = test.spawn(EntityTypes.VILLAGER, 2, 1, 1);
		villager.setHealth(1.0F);
		var zombie = test.spawn(EntityTypes.ZOMBIE, 1, 1, 1);

		test.runAfterDelay(1, () -> zombie.doHurtTarget(level, villager));
		test.succeedWhenEntityPresent(EntityTypes.ZOMBIE_VILLAGER, 2, 1, 1);
	}

	@Override
	public void invokeTestMethod(GameTestHelper test, Method method) throws ReflectiveOperationException {
		method.invoke(this, test);
	}
}
