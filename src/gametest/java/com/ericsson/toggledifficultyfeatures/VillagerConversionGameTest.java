package com.ericsson.toggledifficultyfeatures;

import java.lang.reflect.Method;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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

	@GameTest
	public void globalCuringDiscountCanBeToggled(GameTestHelper test) {
		ServerLevel level = test.getLevel();
		var server = level.getServer();
		Villager villager = test.spawn(EntityTypes.VILLAGER, 1, 1, 1);
		Player playerA = test.makeMockPlayer(GameType.SURVIVAL);
		Player playerB = test.makeMockPlayer(GameType.SURVIVAL);

		villager.getGossips().add(playerA.getUUID(), GossipType.MAJOR_POSITIVE, 20);
		villager.getGossips().add(playerA.getUUID(), GossipType.MINOR_POSITIVE, 25);
		level.getGameRules().set(ToggleDifficultyFeatures.GLOBAL_VILLAGER_DISCOUNTS, false, server);
		test.assertValueEqual(125, villager.getPlayerReputation(playerA), "curer should have the discount");
		test.assertValueEqual(0, villager.getPlayerReputation(playerB), "other players should not have the discount yet");

		level.getGameRules().set(ToggleDifficultyFeatures.GLOBAL_VILLAGER_DISCOUNTS, true, server);
		test.assertValueEqual(125, villager.getPlayerReputation(playerB), "global discounts should apply to other players");

		level.getGameRules().set(ToggleDifficultyFeatures.GLOBAL_VILLAGER_DISCOUNTS, false, server);
		test.assertValueEqual(125, villager.getPlayerReputation(playerA), "curer should keep the discount");
		test.assertValueEqual(0, villager.getPlayerReputation(playerB), "disabling global discounts should revoke them");
		test.succeed();
	}

	@Override
	public void invokeTestMethod(GameTestHelper test, Method method) throws ReflectiveOperationException {
		method.invoke(this, test);
	}
}
