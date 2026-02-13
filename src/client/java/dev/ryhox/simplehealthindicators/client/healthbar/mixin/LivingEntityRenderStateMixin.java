package dev.ryhox.simplehealthindicators.client.healthbar.mixin;

import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements HealthBarRenderStateAccess {

    @Unique private float shi$health;
    @Unique private float shi$maxHealth;
    @Unique private float shi$absorption;

    @Unique private boolean shi$poisoned;
    @Unique private boolean shi$withered;
    @Unique private boolean shi$hasLabel;
    @Unique private boolean shi$hasScoreboardDisplay;

    @Override public float shi$getHealth() { return shi$health; }
    @Override public float shi$getMaxHealth() { return shi$maxHealth; }
    @Override public float shi$getAbsorption() { return shi$absorption; }

    @Override public boolean shi$isPoisoned() { return shi$poisoned; }
    @Override public boolean shi$isWithered() { return shi$withered; }
    @Override public boolean shi$hasLabel() { return shi$hasLabel; }
    @Override public boolean shi$hasScoreboardDisplay() { return shi$hasScoreboardDisplay; }

    @Override public void shi$setHealth(float v) { shi$health = v; }
    @Override public void shi$setMaxHealth(float v) { shi$maxHealth = v; }
    @Override public void shi$setAbsorption(float v) { shi$absorption = v; }

    @Override public void shi$setPoisoned(boolean v) { shi$poisoned = v; }
    @Override public void shi$setWithered(boolean v) { shi$withered = v; }
    @Override public void shi$setHasLabel(boolean v) { shi$hasLabel = v; }
    @Override public void shi$setHasScoreboardDisplay(boolean v) { shi$hasScoreboardDisplay = v; }
}
