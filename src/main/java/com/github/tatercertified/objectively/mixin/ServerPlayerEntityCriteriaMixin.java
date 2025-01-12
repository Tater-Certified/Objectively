package com.github.tatercertified.objectively.mixin;

import com.github.tatercertified.objectively.Objectively;
import com.github.tatercertified.objectively.Objectives;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityCriteriaMixin extends PlayerEntity {
    @Shadow protected abstract void updateScores(ScoreboardCriterion criterion, int score);

    @Shadow private float lastHealthScore;

    @Shadow private int lastFoodScore;

    @Shadow private int lastAirScore;

    @Shadow private int lastArmorScore;

    @Shadow private int lastExperienceScore;

    @Shadow private int lastLevelScore;

    @Shadow private int syncedExperience;

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Shadow public abstract ServerWorld getServerWorld();

    public ServerPlayerEntityCriteriaMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "playerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getHealth()F", ordinal = 3), cancellable = true)
    private void objectively$reduceScoreboardUpdateFrequency(CallbackInfo ci) {
        // This one shouldn't be reduced
        if (this.totalExperience != this.syncedExperience) {
            this.syncedExperience = this.totalExperience;
            this.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
        }
        if (this.age % this.getServerWorld().getGameRules().getInt(Objectively.SCOREBOARD_QUERY_FREQ) == 0) {
            // These are the vanilla ones
            if (this.getHealth() + this.getAbsorptionAmount() != this.lastHealthScore) {
                this.lastHealthScore = this.getHealth() + this.getAbsorptionAmount();
                this.updateScores(ScoreboardCriterion.HEALTH, MathHelper.ceil(this.lastHealthScore));
            }

            if (this.hungerManager.getFoodLevel() != this.lastFoodScore) {
                this.lastFoodScore = this.hungerManager.getFoodLevel();
                this.updateScores(ScoreboardCriterion.FOOD, MathHelper.ceil((float)this.lastFoodScore));
            }

            if (this.getAir() != this.lastAirScore) {
                this.lastAirScore = this.getAir();
                this.updateScores(ScoreboardCriterion.AIR, MathHelper.ceil((float)this.lastAirScore));
            }

            if (this.getArmor() != this.lastArmorScore) {
                this.lastArmorScore = this.getArmor();
                this.updateScores(ScoreboardCriterion.ARMOR, MathHelper.ceil((float)this.lastArmorScore));
            }

            if (this.totalExperience != this.lastExperienceScore) {
                this.lastExperienceScore = this.totalExperience;
                this.updateScores(ScoreboardCriterion.XP, MathHelper.ceil((float)this.lastExperienceScore));
            }

            if (this.experienceLevel != this.lastLevelScore) {
                this.lastLevelScore = this.experienceLevel;
                this.updateScores(ScoreboardCriterion.LEVEL, MathHelper.ceil((float)this.lastLevelScore));
            }

            Criteria.LOCATION.trigger((ServerPlayerEntity)(Object)this);

            // Objectively Objectives
            this.updateScores(Objectives.MAX_HEALTH, MathHelper.ceil(this.getAttributeBaseValue(EntityAttributes.MAX_HEALTH)));
            ci.cancel();
        }
    }
}
