package com.github.tatercertified.objectively;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Objectively implements ModInitializer {
    public static final List<ScoreboardObjective> objectivesOnJoin = new ArrayList<>();
    private static final Path OBJECTIVELY_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("objectively.json");

    public static final GameRules.Key<GameRules.IntRule> SCOREBOARD_QUERY_FREQ = GameRuleRegistry.register("scoreboardQueryFrequency", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(1, 1));

    @Override
    public void onInitialize() {
        Command.register();
        Objectives.init();
        ServerLifecycleEvents.SERVER_STARTING.register(this::loadObjectives);

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.player;
            for (ScoreboardObjective objective : objectivesOnJoin) {
                if (Objectives.notHaveObjective(player, player.getScoreboard(), objective)) {
                    minecraftServer.getScoreboard().getOrCreateScore(serverPlayNetworkHandler.player, objective);
                }
            }
        });
    }

    public static void saveObjectives() {
        List<String> objectiveNames = new ArrayList<>(objectivesOnJoin.size());
        for (ScoreboardObjective objective : objectivesOnJoin) {
            objectiveNames.add(objective.getName());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(objectiveNames);

        try {
            if (Files.notExists(OBJECTIVELY_FILE_PATH)) {
                Files.createFile(OBJECTIVELY_FILE_PATH);
            }
            Files.writeString(OBJECTIVELY_FILE_PATH, json, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadObjectives(MinecraftServer server) {
        Gson gson = new GsonBuilder().create();
        try {
            if (Files.exists(OBJECTIVELY_FILE_PATH)) {
                String json = new String(Files.readAllBytes(OBJECTIVELY_FILE_PATH));
                List<String> objectiveNames = gson.fromJson(json, List.class);

                objectivesOnJoin.addAll(
                        server.getScoreboard().getObjectives().stream()
                                .filter(objective -> objectiveNames.contains(objective.getName()))
                                .toList()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}