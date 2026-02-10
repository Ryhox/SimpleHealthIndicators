package dev.ryhox.simplehealthindicators.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarConfigScreen;
import net.minecraft.client.gui.screen.Screen;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> new HealthBarConfigScreen(parent);
    }
}
