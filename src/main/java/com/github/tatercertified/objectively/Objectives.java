package com.github.tatercertified.objectively;

import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;

public class Objectives {
    public static ScoreboardCriterion MAX_HEALTH = ScoreboardCriterion.create("max_health", true, ScoreboardCriterion.RenderType.HEARTS);

    public static boolean notHaveObjective(ScoreHolder player, Scoreboard scoreboard, ScoreboardObjective objective) {
        return scoreboard.getScore(player, objective) == null;
    }

    public static void init(){}
}
