package dev.ryhox.simplehealthindicators.client.healthbar.mixin;

import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityHealthBarMixin {

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void shi$capture(PlayerLikeEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (entity == null || state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        acc.shi$setHealth(entity.getHealth());
        acc.shi$setMaxHealth(entity.getMaxHealth());
        acc.shi$setAbsorption(entity.getAbsorptionAmount());

        acc.shi$setPoisoned(entity.hasStatusEffect(StatusEffects.POISON));
        acc.shi$setWithered(entity.hasStatusEffect(StatusEffects.WITHER));

        Scoreboard scoreboard = entity.getEntityWorld().getScoreboard();
        ScoreboardObjective belowName = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        boolean hasScoreboardDisplay = false;
        if (belowName != null) {
            ReadableScoreboardScore score = scoreboard.getScore(entity, belowName);
            hasScoreboardDisplay = score != null;
        }
        acc.shi$setHasScoreboardDisplay(hasScoreboardDisplay);
    }
}
