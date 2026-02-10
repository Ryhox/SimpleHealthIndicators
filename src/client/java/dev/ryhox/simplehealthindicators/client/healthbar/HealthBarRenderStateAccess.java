package dev.ryhox.simplehealthindicators.client.healthbar;

public interface HealthBarRenderStateAccess {
    float shi$getHealth();
    float shi$getMaxHealth();
    float shi$getAbsorption();

    boolean shi$isPoisoned();
    boolean shi$isWithered();

    void shi$setHealth(float v);
    void shi$setMaxHealth(float v);
    void shi$setAbsorption(float v);

    void shi$setPoisoned(boolean v);
    void shi$setWithered(boolean v);
}
