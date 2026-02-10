package dev.ryhox.simplehealthindicators.client;

import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarCommand;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarConfig;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarState;
import net.fabricmc.api.ClientModInitializer;

public final class SimplehealthindicatorsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HealthBarState.MODE = HealthBarConfig.loadMode();
        HealthBarCommand.init();
    }
}
