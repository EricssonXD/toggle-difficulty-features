package com.ericsson.toggledifficultyfeatures.mixin;

import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

import com.ericsson.toggledifficultyfeatures.ToggleDifficultyFeatures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "getPlayerReputation", at = @At("RETURN"), cancellable = true)
    private void applyGlobalCuringDiscount(Player player, CallbackInfoReturnable<Integer> cir) {
        Villager villager = (Villager) (Object) this;
        if (!(villager.level() instanceof ServerLevel level)
                || !level.getGameRules().get(ToggleDifficultyFeatures.GLOBAL_VILLAGER_DISCOUNTS)
                || villager.getGossips().getCountForType(GossipType.MAJOR_POSITIVE, value -> value > 0) == 0
                || villager.getGossips().getReputation(player.getUUID(), type -> type == GossipType.MAJOR_POSITIVE) > 0) {
            return;
        }

        int curingReputation = GossipType.MAJOR_POSITIVE.weight * GossipType.MAJOR_POSITIVE.max
                + GossipType.MINOR_POSITIVE.weight * GossipType.MINOR_POSITIVE.max;
        cir.setReturnValue(cir.getReturnValue() + curingReputation);
    }
}
