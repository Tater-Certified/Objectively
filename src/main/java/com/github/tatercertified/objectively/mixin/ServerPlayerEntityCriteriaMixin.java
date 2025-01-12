package com.github.tatercertified.objectively.mixin;

import com.github.tatercertified.objectively.Objectively;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public ServerPlayerEntityCriteriaMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    // TODO Find better injection point for attribute updating to stop it from calling every tick
    @Inject(method = "playerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getHealth()F", ordinal = 3))
    private void objectively$injectCriterion(CallbackInfo ci) {
        this.updateScores(Objectively.MAX_HEALTH, MathHelper.ceil(this.getAttributeBaseValue(EntityAttributes.MAX_HEALTH)));
    }
}
