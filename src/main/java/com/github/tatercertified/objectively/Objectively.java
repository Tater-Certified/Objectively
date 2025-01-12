package com.github.tatercertified.objectively;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;

public class Objectively implements ModInitializer {
    public static ScoreboardCriterion MAX_HEALTH = ScoreboardCriterion.create("max_health", true, ScoreboardCriterion.RenderType.HEARTS);

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            for (ScoreboardObjective objective : minecraftServer.getScoreboard().getObjectives()) {
                // TODO Check if the objective is in a config file
                minecraftServer.getScoreboard().getOrCreateScore(serverPlayNetworkHandler.player, objective);
            }
        });
    }

}
