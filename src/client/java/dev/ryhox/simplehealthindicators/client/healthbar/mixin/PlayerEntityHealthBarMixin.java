package dev.ryhox.simplehealthindicators.client.healthbar.mixin;

import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityHealthBarMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void shi$capture(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        if (entity == null || state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        acc.shi$setHealth(entity.getHealth());
        acc.shi$setMaxHealth(entity.getMaxHealth());
        acc.shi$setAbsorption(entity.getAbsorptionAmount());

        acc.shi$setPoisoned(entity.hasEffect(MobEffects.POISON));
        acc.shi$setWithered(entity.hasEffect(MobEffects.WITHER));

        Scoreboard scoreboard = entity.level().getScoreboard();
        Objective belowName = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
        boolean hasScoreboardDisplay = false;
        if (belowName != null && entity instanceof Player player) {
            ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(player, belowName);
            hasScoreboardDisplay = score != null;
        }
        acc.shi$setHasScoreboardDisplay(hasScoreboardDisplay);
    }
}
