package com.github.tatercertified.objectively;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Command {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("objectively")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective())
                            .then(argument("assign_default", BoolArgumentType.bool()).executes(Command::setDefault))));
        });
    }

    public static int setDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
        boolean setDefault = BoolArgumentType.getBool(context, "assign_default");
        if (setDefault) {
            Objectively.objectivesOnJoin.add(objective);
        } else {
            Objectively.objectivesOnJoin.remove(objective);
        }
        Objectively.saveObjectives();
        return 0;
    }
}
