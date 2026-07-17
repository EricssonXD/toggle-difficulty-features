package com.ericsson.toggledifficultyfeatures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.raid.Raid;

public class RaidGameTest implements CustomTestMethodInvoker {
	@GameTest
	public void enabledEasyRaidUsesSevenWaves(GameTestHelper test) {
		assertWaveCount(test, true, 0, 7);
	}

	@GameTest
	public void disabledEasyRaidUsesThreeWaves(GameTestHelper test) {
		assertWaveCount(test, false, 0, 3);
	}

	@GameTest
	public void raidOmenTwoAddsBonusWave(GameTestHelper test) {
		assertWaveCount(test, true, 2, 8);
	}

	private void assertWaveCount(GameTestHelper test, boolean enabled, int omenLevel, int expected) {
		ServerLevel level = test.getLevel();
		var server = level.getServer();
		server.setDifficulty(Difficulty.EASY, true);
		level.getGameRules().set(ToggleDifficultyFeatures.HARD_MODE_RAID_WAVES, enabled, server);

		Raid raid = new Raid(test.absolutePos(new BlockPos(1, 1, 1)), Difficulty.EASY);
		raid.setRaidOmenLevel(omenLevel);
		raid.tick(level);

		test.assertValueEqual(expected, numGroups(raid),
			"unexpected Easy raid wave count with gamerule=" + enabled + " and omen=" + omenLevel);
		test.succeed();
	}

	private static int numGroups(Raid raid) {
		try {
			Field field = Raid.class.getDeclaredField("numGroups");
			field.setAccessible(true);
			return field.getInt(raid);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not inspect raid wave count", e);
		}
	}

	@Override
	public void invokeTestMethod(GameTestHelper test, Method method) throws ReflectiveOperationException {
		method.invoke(this, test);
	}
}
